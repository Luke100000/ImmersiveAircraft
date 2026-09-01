#!/usr/bin/env node

/**
 * Convert an uncompressed glTF 2.0 binary mesh into the subset of Blockbench's
 * free-model format consumed by Immersive Aircraft 1.20.1.
 *
 * Immersive Aircraft currently renders quad faces only. glTF stores triangles,
 * so each triangle is emitted as a four-point polygon whose last point lies on
 * the closing edge. The second GPU triangle is degenerate, while Blockbench and
 * the mod both retain the complete visible triangle.
 *
 * This script intentionally has no npm dependencies. Optimize a source GLB
 * first (the checked-in source models use glTF-Transform), then provide the
 * extracted base-color texture as a PNG.
 */

import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";

const COMPONENTS = {
  5120: {bytes: 1, read: "getInt8"},
  5121: {bytes: 1, read: "getUint8"},
  5122: {bytes: 2, read: "getInt16"},
  5123: {bytes: 2, read: "getUint16"},
  5125: {bytes: 4, read: "getUint32"},
  5126: {bytes: 4, read: "getFloat32"},
};

const TYPE_COMPONENTS = {
  SCALAR: 1,
  VEC2: 2,
  VEC3: 3,
  VEC4: 4,
  MAT2: 4,
  MAT3: 9,
  MAT4: 16,
};

function usage(message) {
  if (message) console.error(`Error: ${message}\n`);
  console.error(`Usage:
  node glb_to_bbmodel.mjs \\
    --input model.glb \\
    --output model.bbmodel \\
    --texture model.png \\
    --texture-name model.png \\
    --length-blocks 7 \\
    [--forward-sign 1] [--name body] [--extract-texture image.jpg]

The GLB must use ordinary, uncompressed accessors and TRIANGLES primitives.
--forward-sign selects whether source +X (1) or -X (-1) faces Minecraft +Z.`);
  process.exit(2);
}

function parseArgs(argv) {
  const result = {};
  for (let i = 0; i < argv.length; i += 2) {
    const key = argv[i];
    if (!key?.startsWith("--") || argv[i + 1] === undefined) usage(`Invalid argument ${key ?? ""}`);
    result[key.slice(2)] = argv[i + 1];
  }
  for (const required of ["input", "output", "texture", "texture-name", "length-blocks"]) {
    if (!result[required]) usage(`Missing --${required}`);
  }
  result["forward-sign"] ??= "1";
  result.name ??= "body";
  if (!["1", "-1"].includes(result["forward-sign"])) usage("--forward-sign must be 1 or -1");
  if (!(Number(result["length-blocks"]) > 0)) usage("--length-blocks must be positive");
  return result;
}

function readGlb(filename) {
  const file = fs.readFileSync(filename);
  if (file.readUInt32LE(0) !== 0x46546c67) throw new Error(`${filename} is not a GLB file`);
  if (file.readUInt32LE(4) !== 2) throw new Error("Only glTF 2.0 is supported");

  let json;
  let binary;
  let offset = 12;
  while (offset < file.length) {
    const length = file.readUInt32LE(offset);
    const type = file.readUInt32LE(offset + 4);
    const chunk = file.subarray(offset + 8, offset + 8 + length);
    if (type === 0x4e4f534a) json = JSON.parse(chunk.toString("utf8").replace(/\u0000+$/u, ""));
    if (type === 0x004e4942) binary = chunk;
    offset += 8 + length;
  }
  if (!json || !binary) throw new Error("GLB must contain JSON and BIN chunks");
  if (json.extensionsRequired?.length) {
    throw new Error(`Compressed/extended GLB accessors are unsupported: ${json.extensionsRequired.join(", ")}`);
  }
  return {json, binary};
}

function readAccessor(glb, accessorIndex) {
  const {json, binary} = glb;
  const accessor = json.accessors[accessorIndex];
  if (accessor.sparse) throw new Error("Sparse accessors are unsupported");
  const view = json.bufferViews[accessor.bufferView];
  const component = COMPONENTS[accessor.componentType];
  const componentCount = TYPE_COMPONENTS[accessor.type];
  if (!component || !componentCount) throw new Error(`Unsupported accessor ${accessorIndex}`);

  const stride = view.byteStride ?? component.bytes * componentCount;
  const start = (view.byteOffset ?? 0) + (accessor.byteOffset ?? 0);
  const data = new DataView(binary.buffer, binary.byteOffset, binary.byteLength);
  const rows = new Array(accessor.count);
  for (let row = 0; row < accessor.count; row++) {
    const values = new Array(componentCount);
    for (let column = 0; column < componentCount; column++) {
      const byteOffset = start + row * stride + column * component.bytes;
      values[column] = data[component.read](byteOffset, true);
    }
    rows[row] = componentCount === 1 ? values[0] : values;
  }
  return rows;
}

function identity() {
  return [1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1];
}

function multiply(a, b) {
  const out = new Array(16).fill(0);
  for (let column = 0; column < 4; column++) {
    for (let row = 0; row < 4; row++) {
      for (let k = 0; k < 4; k++) out[column * 4 + row] += a[k * 4 + row] * b[column * 4 + k];
    }
  }
  return out;
}

function nodeMatrix(node) {
  if (node.matrix) return node.matrix;
  const [x, y, z, w] = node.rotation ?? [0, 0, 0, 1];
  const [sx, sy, sz] = node.scale ?? [1, 1, 1];
  const [tx, ty, tz] = node.translation ?? [0, 0, 0];
  const x2 = x + x;
  const y2 = y + y;
  const z2 = z + z;
  const xx = x * x2;
  const xy = x * y2;
  const xz = x * z2;
  const yy = y * y2;
  const yz = y * z2;
  const zz = z * z2;
  const wx = w * x2;
  const wy = w * y2;
  const wz = w * z2;
  return [
    (1 - (yy + zz)) * sx, (xy + wz) * sx, (xz - wy) * sx, 0,
    (xy - wz) * sy, (1 - (xx + zz)) * sy, (yz + wx) * sy, 0,
    (xz + wy) * sz, (yz - wx) * sz, (1 - (xx + yy)) * sz, 0,
    tx, ty, tz, 1,
  ];
}

function transformPoint(matrix, point) {
  const [x, y, z] = point;
  return [
    matrix[0] * x + matrix[4] * y + matrix[8] * z + matrix[12],
    matrix[1] * x + matrix[5] * y + matrix[9] * z + matrix[13],
    matrix[2] * x + matrix[6] * y + matrix[10] * z + matrix[14],
  ];
}

function collectPrimitives(glb) {
  const primitives = [];
  const scene = glb.json.scenes[glb.json.scene ?? 0];
  const visit = (nodeIndex, parentMatrix) => {
    const node = glb.json.nodes[nodeIndex];
    const worldMatrix = multiply(parentMatrix, nodeMatrix(node));
    if (node.mesh !== undefined) {
      for (const primitive of glb.json.meshes[node.mesh].primitives) {
        if ((primitive.mode ?? 4) !== 4) throw new Error("Only TRIANGLES primitives are supported");
        if (primitive.indices === undefined || primitive.attributes.POSITION === undefined) {
          throw new Error("Each primitive must contain indices and POSITION");
        }
        const positions = readAccessor(glb, primitive.attributes.POSITION).map((point) => transformPoint(worldMatrix, point));
        const texcoords = primitive.attributes.TEXCOORD_0 === undefined
          ? positions.map(() => [0, 0])
          : readAccessor(glb, primitive.attributes.TEXCOORD_0);
        const indices = readAccessor(glb, primitive.indices);
        primitives.push({positions, texcoords, indices, material: primitive.material ?? 0});
      }
    }
    for (const child of node.children ?? []) visit(child, worldMatrix);
  };
  for (const node of scene.nodes ?? []) visit(node, identity());
  return primitives;
}

function uuid(seed) {
  const hex = crypto.createHash("sha1").update(seed).digest("hex").slice(0, 32).split("");
  hex[12] = "4";
  hex[16] = ((Number.parseInt(hex[16], 16) & 3) | 8).toString(16);
  return `${hex.slice(0, 8).join("")}-${hex.slice(8, 12).join("")}-${hex.slice(12, 16).join("")}-${hex.slice(16, 20).join("")}-${hex.slice(20).join("")}`;
}

function pngSize(buffer) {
  if (buffer.length < 24 || buffer.toString("ascii", 1, 4) !== "PNG") throw new Error("--texture must be a PNG");
  return [buffer.readUInt32BE(16), buffer.readUInt32BE(20)];
}

function extractBaseColor(glb, filename) {
  const material = glb.json.materials?.find((entry) => entry.pbrMetallicRoughness?.baseColorTexture);
  if (!material) throw new Error("No base-color texture was found in the GLB");
  const texture = glb.json.textures[material.pbrMetallicRoughness.baseColorTexture.index];
  const image = glb.json.images[texture.source];
  if (image.bufferView === undefined) throw new Error("Only GLB-embedded images can be extracted");
  const view = glb.json.bufferViews[image.bufferView];
  const start = view.byteOffset ?? 0;
  fs.mkdirSync(path.dirname(filename), {recursive: true});
  fs.writeFileSync(filename, glb.binary.subarray(start, start + view.byteLength));
}

function round(value) {
  const rounded = Math.round(value * 1e6) / 1e6;
  return Object.is(rounded, -0) ? 0 : rounded;
}

function main() {
  const args = parseArgs(process.argv.slice(2));
  const glb = readGlb(args.input);
  if (args["extract-texture"]) extractBaseColor(glb, args["extract-texture"]);

  const primitives = collectPrimitives(glb);
  if (!primitives.length) throw new Error("No mesh primitives were found");
  const forwardSign = Number(args["forward-sign"]);
  const orient = ([x, y, z]) => forwardSign > 0 ? [-z, y, x] : [z, y, -x];
  const allPositions = primitives.flatMap((primitive) => primitive.positions.map(orient));
  const minimum = [Infinity, Infinity, Infinity];
  const maximum = [-Infinity, -Infinity, -Infinity];
  for (const point of allPositions) {
    for (let axis = 0; axis < 3; axis++) {
      minimum[axis] = Math.min(minimum[axis], point[axis]);
      maximum[axis] = Math.max(maximum[axis], point[axis]);
    }
  }
  const forwardExtent = maximum[2] - minimum[2];
  if (!(forwardExtent > 0)) throw new Error("The model has no length on its selected forward axis");
  const scale = Number(args["length-blocks"]) * 16 / forwardExtent;
  const centerX = (minimum[0] + maximum[0]) / 2;
  const centerZ = (minimum[2] + maximum[2]) / 2;
  const place = (point) => {
    const [x, y, z] = orient(point);
    return [round((x - centerX) * scale), round((y - minimum[1]) * scale), round((z - centerZ) * scale)];
  };

  const textureData = fs.readFileSync(args.texture);
  const [textureWidth, textureHeight] = pngSize(textureData);
  const modelSeed = path.basename(args.output);
  const textureUuid = uuid(`${modelSeed}:texture`);
  const elements = [];
  const outliner = [];
  let triangleCount = 0;

  primitives.forEach((primitive, primitiveIndex) => {
    const elementUuid = uuid(`${modelSeed}:element:${primitiveIndex}`);
    const vertices = {};
    const faces = {};
    primitive.positions.forEach((point, index) => {
      vertices[`v${index}`] = place(point);
    });

    for (let i = 0; i + 2 < primitive.indices.length; i += 3) {
      const ia = primitive.indices[i];
      const ib = primitive.indices[i + 1];
      const ic = primitive.indices[i + 2];
      const a = vertices[`v${ia}`];
      const b = vertices[`v${ib}`];
      const c = vertices[`v${ic}`];
      const ab = [b[0] - a[0], b[1] - a[1], b[2] - a[2]];
      const ac = [c[0] - a[0], c[1] - a[1], c[2] - a[2]];
      const cross = [ab[1] * ac[2] - ab[2] * ac[1], ab[2] * ac[0] - ab[0] * ac[2], ab[0] * ac[1] - ab[1] * ac[0]];
      if (cross[0] * cross[0] + cross[1] * cross[1] + cross[2] * cross[2] < 1e-12) continue;

      const midpointKey = `m${triangleCount}`;
      vertices[midpointKey] = [round((a[0] + c[0]) / 2), round((a[1] + c[1]) / 2), round((a[2] + c[2]) / 2)];
      const keys = [`v${ia}`, `v${ib}`, `v${ic}`, midpointKey];
      const uva = primitive.texcoords[ia] ?? [0, 0];
      const uvb = primitive.texcoords[ib] ?? [0, 0];
      const uvc = primitive.texcoords[ic] ?? [0, 0];
      const uvm = [(uva[0] + uvc[0]) / 2, (uva[1] + uvc[1]) / 2];
      // glTF and Blockbench both define (0, 0) at the upper-left of the image.
      // Flipping V here points each face at an unrelated mirrored atlas island.
      const toUv = ([u, v]) => [round(u * textureWidth), round(v * textureHeight)];
      faces[`f${triangleCount}`] = {
        uv: {
          [keys[0]]: toUv(uva),
          [keys[1]]: toUv(uvb),
          [keys[2]]: toUv(uvc),
          [keys[3]]: toUv(uvm),
        },
        vertices: keys,
        texture: 0,
      };
      triangleCount++;
    }

    elements.push({
      name: primitives.length === 1 ? args.name : `${args.name}_${primitiveIndex + 1}`,
      color: primitiveIndex % 8,
      origin: [0, 0, 0],
      rotation: [0, 0, 0],
      export: true,
      visibility: true,
      locked: false,
      render_order: "default",
      allow_mirror_modeling: true,
      vertices,
      faces,
      type: "mesh",
      uuid: elementUuid,
    });
    outliner.push(elementUuid);
  });

  const output = {
    meta: {format_version: "4.10", model_format: "free", box_uv: false},
    name: path.basename(args.output, path.extname(args.output)),
    model_identifier: "",
    visible_box: [1, 1, 0],
    variable_placeholders: "",
    resolution: {width: textureWidth, height: textureHeight},
    elements,
    outliner,
    textures: [{
      path: "",
      name: args["texture-name"],
      folder: "",
      namespace: "",
      id: "0",
      particle: false,
      render_mode: "default",
      render_sides: "auto",
      frame_time: 1,
      frame_order_type: "loop",
      frame_order: "",
      frame_interpolate: false,
      visible: true,
      internal: true,
      saved: true,
      uuid: textureUuid,
      relative_path: `../textures/entity/${args["texture-name"]}`,
      source: `data:image/png;base64,${textureData.toString("base64")}`,
      width: textureWidth,
      height: textureHeight,
      uv_width: textureWidth,
      uv_height: textureHeight,
    }],
    animations: [],
  };

  fs.mkdirSync(path.dirname(args.output), {recursive: true});
  fs.writeFileSync(args.output, `${JSON.stringify(output)}\n`);
  const widthBlocks = (maximum[0] - minimum[0]) * scale / 16;
  const heightBlocks = (maximum[1] - minimum[1]) * scale / 16;
  console.log(JSON.stringify({
    output: args.output,
    triangles: triangleCount,
    dimensions_blocks: {
      width: round(widthBlocks),
      height: round(heightBlocks),
      length: Number(args["length-blocks"]),
    },
    texture: `${textureWidth}x${textureHeight}`,
  }, null, 2));
}

main();
