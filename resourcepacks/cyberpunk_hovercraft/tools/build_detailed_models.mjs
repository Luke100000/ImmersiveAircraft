#!/usr/bin/env node

/** Rebuild both Minecraft-native vehicle skin models with img2blockbench. */

import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import {spawnSync} from "node:child_process";
import {fileURLToPath} from "node:url";

const TOOL_DIR = path.dirname(fileURLToPath(import.meta.url));
const PACK_DIR = path.resolve(TOOL_DIR, "..");
const SOURCE_DIR = path.join(PACK_DIR, "source");
const TEXTURE_DIR = path.join(PACK_DIR, "assets/immersive_aircraft/textures/entity");
const OBJECT_DIR = path.join(PACK_DIR, "assets/immersive_aircraft/objects/vehicle_skins");
const COMPILER = process.env.IMG2BLOCKBENCH ?? "img2blockbench";

function run(command, args) {
  const result = spawnSync(command, args, {stdio: "inherit"});
  if (result.error) throw result.error;
  if (result.status !== 0) throw new Error(`${command} failed with status ${result.status}`);
}

function build(modelId, vehicleDirectory) {
  const spec = path.join(SOURCE_DIR, `${modelId}.model-spec.json`);
  const temporary = fs.mkdtempSync(path.join(os.tmpdir(), `${modelId}-`));
  try {
    run(COMPILER, ["validate", "--strict", spec]);
    run(COMPILER, ["build", spec, "--output", temporary]);

    const modelDirectory = path.join(OBJECT_DIR, vehicleDirectory);
    fs.mkdirSync(modelDirectory, {recursive: true});
    fs.copyFileSync(path.join(temporary, `${modelId}.bbmodel`), path.join(modelDirectory, `${modelId}.bbmodel`));
    fs.copyFileSync(path.join(temporary, `${modelId}.png`), path.join(TEXTURE_DIR, `${modelId}.png`));
    fs.copyFileSync(path.join(temporary, `${modelId}.audit.json`), path.join(SOURCE_DIR, `${modelId}.audit.json`));
  } finally {
    fs.rmSync(temporary, {recursive: true, force: true});
  }
}

fs.mkdirSync(TEXTURE_DIR, {recursive: true});
build("militech_av", "airship");
build("trauma_atlus", "cargo_airship");
console.log(JSON.stringify({models: ["militech_av", "trauma_atlus"]}, null, 2));
