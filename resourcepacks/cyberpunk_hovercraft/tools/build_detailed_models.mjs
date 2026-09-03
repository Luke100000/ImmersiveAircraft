#!/usr/bin/env node

/** Rebuild the detailed runtime models from the supplied optimized GLBs. */

import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import {spawnSync} from "node:child_process";
import {fileURLToPath} from "node:url";

const TOOL_DIR = path.dirname(fileURLToPath(import.meta.url));
const PACK_DIR = path.resolve(TOOL_DIR, "..");
const SOURCE_DIR = path.join(PACK_DIR, "source");
const TEXTURE_DIR = path.join(PACK_DIR, "assets/immersive_aircraft/textures/entity");
const OBJECT_DIR = path.join(PACK_DIR, "assets/immersive_aircraft/objects");
const BUILD_SCALE = 1.7;

function run(command, args) {
  const result = spawnSync(command, args, {stdio: "inherit"});
  if (result.status !== 0) throw new Error(`${command} failed with status ${result.status}`);
}

function makeFillTexture(output, body, recess, accent) {
  const width = 64;
  const height = 32;
  const pixels = Buffer.alloc(width * height * 3);
  const set = (x, y, color) => {
    const offset = (y * width + x) * 3;
    pixels[offset] = color[0];
    pixels[offset + 1] = color[1];
    pixels[offset + 2] = color[2];
  };
  const shade = (color, amount) => color.map((value) => Math.max(0, Math.min(255, value + amount)));
  for (let tile = 0; tile < 2; tile++) {
    const x0 = tile * 32;
    const base = tile === 0 ? body : recess;
    for (let y = 0; y < height; y++) {
      const gradient = Math.round((0.5 - y / (height - 1)) * 12);
      for (let x = x0; x < x0 + 32; x++) set(x, y, shade(base, gradient));
    }
    for (let x = x0; x < x0 + 32; x++) {
      set(x, 0, shade(base, 30));
      set(x, 31, shade(base, -32));
    }
    for (let y = 0; y < 32; y++) {
      set(x0, y, shade(base, 18));
      set(x0 + 31, y, shade(base, -36));
    }
    for (const [x, y] of [[4, 4], [27, 4], [4, 27], [27, 27]]) {
      set(x0 + x, y, accent);
      set(x0 + x + 1, y, accent);
      set(x0 + x, y + 1, accent);
      set(x0 + x + 1, y + 1, accent);
    }
  }
  const ppm = Buffer.concat([Buffer.from(`P6\n${width} ${height}\n255\n`), pixels]);
  const temporary = path.join(os.tmpdir(), `${path.basename(output)}.ppm`);
  fs.writeFileSync(temporary, ppm);
  run("ffmpeg", ["-hide_banner", "-loglevel", "error", "-y", "-i", temporary, "-frames:v", "1", "-pix_fmt", "rgb24", output]);
  fs.rmSync(temporary);
}

function build(profile, modelName, sourceGlb, sourceTexture, panelTexture, fillTexture, fillColors) {
  run("ffmpeg", [
    "-hide_banner", "-loglevel", "error", "-y", "-i", sourceTexture,
    "-vf", "bilateral=sigmaS=2:sigmaR=0.06:planes=7", "-frames:v", "1", "-pix_fmt", "rgb24", panelTexture,
  ]);
  makeFillTexture(fillTexture, ...fillColors);

  const temporary = fs.mkdtempSync(path.join(os.tmpdir(), `${profile}-`));
  const flat = path.join(temporary, `${modelName}-flat.bbmodel`);
  try {
    run(process.execPath, [
      path.join(TOOL_DIR, "glb_to_bbmodel.mjs"),
      "--input", sourceGlb,
      "--output", flat,
      "--texture", panelTexture,
      "--texture-name", path.basename(panelTexture),
      "--length-blocks", "8",
      "--forward-sign", "-1",
      "--name", "body",
    ]);
    run(process.execPath, [
      path.join(TOOL_DIR, "rig_bbmodel.mjs"),
      "--input", flat,
      "--output", path.join(OBJECT_DIR, `${modelName}.bbmodel`),
      "--profile", profile,
      "--fill-texture", fillTexture,
      "--fill-texture-name", path.basename(fillTexture),
      "--scale", String(BUILD_SCALE),
    ]);
  } finally {
    fs.rmSync(temporary, {recursive: true, force: true});
  }
}

fs.mkdirSync(TEXTURE_DIR, {recursive: true});
fs.mkdirSync(OBJECT_DIR, {recursive: true});

build(
  "militech_av", "airship",
  path.join(SOURCE_DIR, "militech_av.glb"),
  path.join(SOURCE_DIR, "militech_av.png"),
  path.join(TEXTURE_DIR, "militech_av_panels.png"),
  path.join(TEXTURE_DIR, "militech_av_fill.png"),
  [[45, 50, 54], [17, 22, 25], [218, 146, 28]],
);
build(
  "trauma_atlus", "cargo_airship",
  path.join(SOURCE_DIR, "trauma_atlus.glb"),
  path.join(SOURCE_DIR, "trauma_atlus.png"),
  path.join(TEXTURE_DIR, "trauma_atlus_panels.png"),
  path.join(TEXTURE_DIR, "trauma_atlus_fill.png"),
  [[210, 214, 211], [18, 118, 121], [168, 30, 41]],
);

console.log(JSON.stringify({scale: BUILD_SCALE, models: ["airship", "cargo_airship"]}, null, 2));
