#!/usr/bin/env node

/**
 * Split a GLB-derived mesh into editable aircraft parts, seal hidden gaps, and
 * add pod articulation. The supplied mesh and UVs stay intact; the only added
 * geometry is recessed backing/cap geometry that prevents see-through holes.
 */

import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";

const SIDES = ["north", "east", "south", "west", "up", "down"];

function usage(message) {
  if (message) console.error(`Error: ${message}\n`);
  console.error(`Usage:
  node rig_bbmodel.mjs \\
    --input flat.bbmodel \\
    --output rigged.bbmodel \\
    --profile militech_av|trauma_atlus \\
    --fill-texture panel_fill.png \\
    --fill-texture-name panel_fill.png \\
    [--scale 1.7]`);
  process.exit(2);
}

function parseArgs(argv) {
  const args = {scale: "1.7"};
  for (let index = 0; index < argv.length; index += 2) {
    const key = argv[index];
    if (!key?.startsWith("--") || argv[index + 1] === undefined) usage(`Invalid argument ${key ?? ""}`);
    args[key.slice(2)] = argv[index + 1];
  }
  for (const key of ["input", "output", "profile", "fill-texture", "fill-texture-name"]) {
    if (!args[key]) usage(`Missing --${key}`);
  }
  if (!(Number(args.scale) > 0)) usage("--scale must be positive");
  return args;
}

function uuid(seed) {
  const hex = crypto.createHash("sha1").update(seed).digest("hex").slice(0, 32).split("");
  hex[12] = "4";
  hex[16] = ((Number.parseInt(hex[16], 16) & 3) | 8).toString(16);
  return `${hex.slice(0, 8).join("")}-${hex.slice(8, 12).join("")}-${hex.slice(12, 16).join("")}-${hex.slice(16, 20).join("")}-${hex.slice(20).join("")}`;
}

function round(value) {
  const result = Math.round(value * 1e6) / 1e6;
  return Object.is(result, -0) ? 0 : result;
}

function sideName(x) {
  return x < 0 ? "left" : "right";
}

const PROFILES = {
  militech_av: {
    parts: [
      "hull", "cockpit", "roof", "rear_hull", "landing_gear",
      "left_door", "right_door",
      "left_front_vtol", "right_front_vtol", "left_rear_vtol", "right_rear_vtol",
    ],
    classify([x, y, z]) {
      const ax = Math.abs(x);
      const side = sideName(x);
      if (ax > 1.08 && z < -2.05) return `${side}_rear_vtol`;
      if (ax > 1.02 && z > 1.75 && y < 1.9) return `${side}_front_vtol`;
      if (ax > 1.12 && z > -1.55 && z < 1.45 && y > 0.35 && y < 1.9) return `${side}_door`;
      if (y < 0.34 && ax > 0.55) return "landing_gear";
      if (z > 2.15 && y > 0.55) return "cockpit";
      if (y > 1.78 && z > -2.15 && z < 2.25) return "roof";
      if (z < -1.9) return "rear_hull";
      return "hull";
    },
    pods: [
      {name: "left_front_vtol", pivot: [-1.38, 0.95, 2.55], cap: [-1.38, 1.48, 2.55], radius: 0.29},
      {name: "right_front_vtol", pivot: [1.38, 0.95, 2.55], cap: [1.38, 1.48, 2.55], radius: 0.29},
      {name: "left_rear_vtol", pivot: [-1.58, 1.14, -3.12], cap: [-1.58, 1.82, -3.12], radius: 0.33},
      {name: "right_rear_vtol", pivot: [1.58, 1.14, -3.12], cap: [1.58, 1.82, -3.12], radius: 0.33},
    ],
    pivots: {
      cockpit: [0, 1.15, 3.1], roof: [0, 1.82, 0], rear_hull: [0, 1.2, -3],
      landing_gear: [0, 0.25, 0], left_door: [-1.18, 1.7, 0], right_door: [1.18, 1.7, 0],
    },
    fillers: [
      {group: "hull", name: "sealed_inner_hull", from: [-0.72, 0.44, -2.48], to: [0.72, 1.55, 2.02], tile: 0},
      {group: "left_door", name: "left_door_backing", from: [-1.16, 0.5, -1.32], to: [-1.06, 1.72, 1.24], tile: 0},
      {group: "right_door", name: "right_door_backing", from: [1.06, 0.5, -1.32], to: [1.16, 1.72, 1.24], tile: 0},
    ],
  },
  trauma_atlus: {
    parts: [
      "hull", "cockpit", "roof", "rear_deck", "landing_gear",
      "left_door", "right_door",
      "left_front_vtol", "right_front_vtol", "left_rear_vtol", "right_rear_vtol",
    ],
    classify([x, y, z]) {
      const ax = Math.abs(x);
      const side = sideName(x);
      if (ax > 1.28 && z > 1.2) return `${side}_front_vtol`;
      if (ax > 1.28 && z < -1.45) return `${side}_rear_vtol`;
      if (ax > 1.16 && z > -1.45 && z < 1.2 && y > 0.55 && y < 2.55) return `${side}_door`;
      if (y < 0.58 && ax > 0.7) return "landing_gear";
      if (z > 2.05 && ax < 1.5) return "cockpit";
      if (y > 2.35 && z > -2.15 && z < 2.1) return "roof";
      if (z < -2.15) return "rear_deck";
      return "hull";
    },
    pods: [
      {name: "left_front_vtol", pivot: [-1.78, 1.1, 2.2], cap: [-1.78, 1.82, 2.2], radius: 0.37},
      {name: "right_front_vtol", pivot: [1.78, 1.1, 2.2], cap: [1.78, 1.82, 2.2], radius: 0.37},
      {name: "left_rear_vtol", pivot: [-1.78, 1.25, -2.35], cap: [-1.78, 1.94, -2.35], radius: 0.37},
      {name: "right_rear_vtol", pivot: [1.78, 1.25, -2.35], cap: [1.78, 1.94, -2.35], radius: 0.37},
    ],
    pivots: {
      cockpit: [0, 1.35, 3], roof: [0, 2.4, 0], rear_deck: [0, 1.6, -2.7],
      landing_gear: [0, 0.35, 0], left_door: [-1.28, 2.15, 0], right_door: [1.28, 2.15, 0],
    },
    fillers: [
      {group: "hull", name: "sealed_inner_hull", from: [-0.82, 0.48, -2.52], to: [0.82, 2.08, 1.92], tile: 0},
      {group: "left_door", name: "left_door_backing", from: [-1.28, 0.64, -1.28], to: [-1.18, 2.28, 1.16], tile: 0},
      {group: "right_door", name: "right_door_backing", from: [1.18, 0.64, -1.28], to: [1.28, 2.28, 1.16], tile: 0},
    ],
  },
};

function scaledPoint(point, scale) {
  return point.map((value) => round(value * scale));
}

function makeBone(seed, name, originBlocks, children, scale) {
  return {
    name, origin: originBlocks.map((value) => round(value * 16 * scale)), rotation: [0, 0, 0], color: 0,
    uuid: uuid(`${seed}:bone:${name}`), export: true, mirror_uv: false, isOpen: true,
    locked: false, visibility: true, autouv: 0, children,
  };
}

function makeMesh(source, seed, name, color, faceEntries, scale) {
  const vertices = {};
  const faces = {};
  for (const [faceId, face] of faceEntries) {
    faces[faceId] = face;
    for (const vertexId of face.vertices) vertices[vertexId] = scaledPoint(source.vertices[vertexId], scale);
  }
  return {
    ...source, name, color, origin: [0, 0, 0], rotation: [0, 0, 0], vertices, faces,
    uuid: uuid(`${seed}:mesh:${name}`),
  };
}

function makeCube(seed, definition, scale) {
  const uv = definition.tile === 0 ? [2, 2, 30, 30] : [34, 2, 62, 30];
  return {
    name: definition.name, box_uv: false, rescale: false, locked: false, render_order: "default",
    allow_mirror_modeling: true,
    from: definition.from.map((value) => round(value * 16 * scale)),
    to: definition.to.map((value) => round(value * 16 * scale)),
    autouv: 0, color: 7, origin: [0, 0, 0], rotation: [0, 0, 0],
    faces: Object.fromEntries(SIDES.map((side) => [side, {uv, texture: 1}])),
    type: "cube", uuid: uuid(`${seed}:cube:${definition.name}`),
  };
}

function makeRotationAnimator(seed, name, expression) {
  return {
    name, type: "bone", keyframes: [{
      channel: "rotation", data_points: [{x: expression, y: "0", z: "0"}],
      uuid: uuid(`${seed}:keyframe:${name}`), time: 0, color: -1, interpolation: "linear",
    }],
  };
}

function pngSize(buffer) {
  if (buffer.length < 24 || buffer.toString("ascii", 1, 4) !== "PNG") throw new Error("Fill texture must be PNG");
  return [buffer.readUInt32BE(16), buffer.readUInt32BE(20)];
}

function makeTexture(filename, textureName, seed) {
  const data = fs.readFileSync(filename);
  const [width, height] = pngSize(data);
  return {
    path: "", name: textureName, folder: "", namespace: "", id: "1", particle: false,
    render_mode: "default", render_sides: "auto", frame_time: 1, frame_order_type: "loop",
    frame_order: "", frame_interpolate: false, visible: true, internal: true, saved: true,
    uuid: uuid(`${seed}:texture:fill`), relative_path: `../textures/entity/${textureName}`,
    source: `data:image/png;base64,${data.toString("base64")}`, width, height, uv_width: width, uv_height: height,
  };
}

function main() {
  const args = parseArgs(process.argv.slice(2));
  const profile = PROFILES[args.profile];
  if (!profile) usage(`Unknown --profile ${args.profile}`);
  const scale = Number(args.scale);
  const model = JSON.parse(fs.readFileSync(args.input, "utf8"));
  const meshes = model.elements.filter((element) => element.type === "mesh");
  if (meshes.length !== 1 || model.elements.length !== 1) throw new Error("Input must be one flat mesh from glb_to_bbmodel.mjs");

  const source = meshes[0];
  const seed = `cyberpunk_hovercraft:detailed:${args.profile}`;
  const assignments = new Map(profile.parts.map((name) => [name, []]));
  for (const entry of Object.entries(source.faces)) {
    const points = entry[1].vertices.slice(0, 3).map((id) => source.vertices[id]);
    const center = [0, 1, 2].map((axis) => points.reduce((sum, point) => sum + point[axis], 0) / points.length / 16);
    assignments.get(profile.classify(center)).push(entry);
  }

  const elements = [];
  const childrenByPart = new Map(profile.parts.map((name) => [name, []]));
  profile.parts.forEach((name, index) => {
    const entries = assignments.get(name);
    if (!entries.length) return;
    const element = makeMesh(source, seed, name, index % 8, entries, scale);
    elements.push(element);
    childrenByPart.get(name).push(element.uuid);
  });
  for (const filler of profile.fillers) {
    const element = makeCube(seed, filler, scale);
    elements.push(element);
    childrenByPart.get(filler.group).push(element.uuid);
  }
  for (const pod of profile.pods) {
    const [x, y, z] = pod.cap;
    const cap = makeCube(seed, {
      group: pod.name, name: `${pod.name}_sealed_cap`, tile: 1,
      from: [x - pod.radius, y - 0.025, z - pod.radius],
      to: [x + pod.radius, y + 0.025, z + pod.radius],
    }, scale);
    elements.push(cap);
    childrenByPart.get(pod.name).push(cap.uuid);
  }

  const podMap = new Map(profile.pods.map((pod) => [pod.name, pod]));
  const animators = {};
  const partBones = [];
  for (const name of profile.parts) {
    const pod = podMap.get(name);
    const origin = pod?.pivot ?? profile.pivots[name] ?? [0, 0, 0];
    const bone = makeBone(seed, name, origin, childrenByPart.get(name), scale);
    partBones.push(bone);
    if (pod) animators[bone.uuid] = makeRotationAnimator(seed, name, "variable.pressing_interpolated_z * -8");
  }

  model.elements = elements;
  model.outliner = [makeBone(seed, "airframe", [0, 0, 0], partBones, scale)];
  model.textures.push(makeTexture(args["fill-texture"], args["fill-texture-name"], seed));
  model.animations = [{
    uuid: uuid(`${seed}:animation:flight`), name: "flight", loop: "loop", override: false,
    length: 0, snapping: 24, selected: false, anim_time_update: "", blend_weight: "",
    start_delay: "", loop_delay: "", animators,
  }];
  model.animation_variable_placeholders = "variable.pressing_interpolated_z=1";
  model.name = path.basename(args.output, path.extname(args.output));
  fs.mkdirSync(path.dirname(args.output), {recursive: true});
  fs.writeFileSync(args.output, `${JSON.stringify(model)}\n`);

  console.log(JSON.stringify({
    output: args.output, profile: args.profile, scale,
    sourceFaces: Object.keys(source.faces).length,
    outputFaces: elements.reduce((sum, element) => sum + Object.keys(element.faces).length, 0),
    meshParts: profile.parts.length, fillerCubes: profile.fillers.length + profile.pods.length,
    bones: partBones.length + 1, animators: Object.keys(animators).length,
  }, null, 2));
}

main();
