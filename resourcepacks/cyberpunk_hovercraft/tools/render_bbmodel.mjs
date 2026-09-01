#!/usr/bin/env node

/** Render a textured Blockbench free model to a transparent PNG using ffmpeg. */

import fs from "node:fs";
import path from "node:path";
import {spawnSync} from "node:child_process";

function parseArgs(argv) {
  const args = {size: "512", yaw: "35", elevation: "24"};
  for (let i = 0; i < argv.length; i += 2) args[argv[i].replace(/^--/u, "")] = argv[i + 1];
  for (const key of ["model", "texture", "output"]) {
    if (!args[key]) throw new Error(`Missing --${key}`);
  }
  return args;
}

function pngSize(buffer) {
  if (buffer.toString("ascii", 1, 4) !== "PNG") throw new Error("Texture must be PNG");
  return [buffer.readUInt32BE(16), buffer.readUInt32BE(20)];
}

const add = (a, b) => a.map((value, i) => value + b[i]);
const subtract = (a, b) => a.map((value, i) => value - b[i]);
const scale = (a, factor) => a.map((value) => value * factor);
const dot = (a, b) => a.reduce((sum, value, i) => sum + value * b[i], 0);
const cross = (a, b) => [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]];
const normalize = (a) => scale(a, 1 / Math.hypot(...a));

function main() {
  const args = parseArgs(process.argv.slice(2));
  const size = Number(args.size);
  const model = JSON.parse(fs.readFileSync(args.model, "utf8"));
  const textureFile = fs.readFileSync(args.texture);
  const [textureWidth, textureHeight] = pngSize(textureFile);
  const decoded = spawnSync("ffmpeg", [
    "-hide_banner", "-loglevel", "error", "-i", args.texture,
    "-f", "rawvideo", "-pix_fmt", "rgba", "pipe:1",
  ], {maxBuffer: textureWidth * textureHeight * 8});
  if (decoded.status !== 0) throw new Error(decoded.stderr.toString());
  const texture = decoded.stdout;

  const triangles = [];
  for (const element of model.elements ?? []) {
    if (element.type !== "mesh") continue;
    const origin = element.origin ?? [0, 0, 0];
    for (const face of Object.values(element.faces ?? {})) {
      if (!face.vertices || face.vertices.length < 3 || face.texture === null) continue;
      const keys = face.vertices.slice(0, 3);
      const points = keys.map((key) => add(element.vertices[key], origin).map((value) => value / 16));
      const uvs = keys.map((key) => face.uv[key]);
      triangles.push({points, uvs});
    }
  }
  if (!triangles.length) throw new Error("No textured mesh triangles found");

  const allPoints = triangles.flatMap((triangle) => triangle.points);
  const minimum = [0, 1, 2].map((axis) => Math.min(...allPoints.map((point) => point[axis])));
  const maximum = [0, 1, 2].map((axis) => Math.max(...allPoints.map((point) => point[axis])));
  const center = scale(add(minimum, maximum), 0.5);
  const yaw = Number(args.yaw) * Math.PI / 180;
  const elevation = Number(args.elevation) * Math.PI / 180;
  const cameraDirection = normalize([
    Math.sin(yaw) * Math.cos(elevation),
    Math.sin(elevation),
    Math.cos(yaw) * Math.cos(elevation),
  ]);
  const right = normalize(cross([0, 1, 0], cameraDirection));
  const up = normalize(cross(cameraDirection, right));
  const project = (point) => {
    const relative = subtract(point, center);
    return [dot(relative, right), dot(relative, up), dot(relative, cameraDirection)];
  };
  const projected = allPoints.map(project);
  const minX = Math.min(...projected.map((point) => point[0]));
  const maxX = Math.max(...projected.map((point) => point[0]));
  const minY = Math.min(...projected.map((point) => point[1]));
  const maxY = Math.max(...projected.map((point) => point[1]));
  const imageScale = 0.86 * size / Math.max(maxX - minX, maxY - minY);
  const projectPixel = (point) => {
    const [x, y, depth] = project(point);
    return [(x - (minX + maxX) / 2) * imageScale + size / 2, size / 2 - (y - (minY + maxY) / 2) * imageScale, depth];
  };

  const output = Buffer.alloc(size * size * 4);
  const depthBuffer = new Float64Array(size * size);
  depthBuffer.fill(-Infinity);
  const light = normalize([-0.4, 0.8, 0.6]);
  const edge = (a, b, p) => (p[0] - a[0]) * (b[1] - a[1]) - (p[1] - a[1]) * (b[0] - a[0]);

  for (const triangle of triangles) {
    const points = triangle.points.map(projectPixel);
    const area = edge(points[0], points[1], points[2]);
    if (Math.abs(area) < 1e-8) continue;
    const x0 = Math.max(0, Math.floor(Math.min(...points.map((point) => point[0]))));
    const x1 = Math.min(size - 1, Math.ceil(Math.max(...points.map((point) => point[0]))));
    const y0 = Math.max(0, Math.floor(Math.min(...points.map((point) => point[1]))));
    const y1 = Math.min(size - 1, Math.ceil(Math.max(...points.map((point) => point[1]))));
    const normal = normalize(cross(subtract(triangle.points[1], triangle.points[0]), subtract(triangle.points[2], triangle.points[0])));
    const shade = 0.52 + 0.48 * Math.abs(dot(normal, light));

    for (let y = y0; y <= y1; y++) {
      for (let x = x0; x <= x1; x++) {
        const sample = [x + 0.5, y + 0.5];
        const w0 = edge(points[1], points[2], sample) / area;
        const w1 = edge(points[2], points[0], sample) / area;
        const w2 = 1 - w0 - w1;
        if (w0 < -1e-6 || w1 < -1e-6 || w2 < -1e-6) continue;
        const depth = w0 * points[0][2] + w1 * points[1][2] + w2 * points[2][2];
        const pixel = y * size + x;
        if (depth <= depthBuffer[pixel]) continue;
        depthBuffer[pixel] = depth;

        const u = w0 * triangle.uvs[0][0] + w1 * triangle.uvs[1][0] + w2 * triangle.uvs[2][0];
        const v = w0 * triangle.uvs[0][1] + w1 * triangle.uvs[1][1] + w2 * triangle.uvs[2][1];
        const tx = Math.max(0, Math.min(textureWidth - 1, Math.round(u)));
        const ty = Math.max(0, Math.min(textureHeight - 1, Math.round(v)));
        const source = (ty * textureWidth + tx) * 4;
        const target = pixel * 4;
        output[target] = Math.min(255, Math.round(texture[source] * shade));
        output[target + 1] = Math.min(255, Math.round(texture[source + 1] * shade));
        output[target + 2] = Math.min(255, Math.round(texture[source + 2] * shade));
        output[target + 3] = texture[source + 3];
      }
    }
  }

  fs.mkdirSync(path.dirname(args.output), {recursive: true});
  const encoded = spawnSync("ffmpeg", [
    "-hide_banner", "-loglevel", "error", "-y",
    "-f", "rawvideo", "-pixel_format", "rgba", "-video_size", `${size}x${size}`,
    "-i", "pipe:0", "-frames:v", "1", args.output,
  ], {input: output, maxBuffer: size * size * 8});
  if (encoded.status !== 0) throw new Error(encoded.stderr.toString());
  console.log(`${args.output} ${size}x${size} (${triangles.length} triangles)`);
}

main();
