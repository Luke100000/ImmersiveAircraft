# Immersive Aircraft

This mod adds bunch of rustic aircraft to travel, transport, and explore! The aircraft have a strong focus on being vanilla-faithful and many details and functionalities, without being overly complicated.

[![Crowdin](https://badges.crowdin.net/immersive-collection/localized.svg)](https://crowdin.com/project/immersive-collection)

Hosted on
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/immersive-aircraft) and
[Modrinth](https://modrinth.com/mod/immersive-aircraft)

## 1.12.2 backport

This branch is a backport of the mod (based on the 1.16.5 codebase, with the
1.20.1 content — Warship, Bamboo Hopper and the weapon system — ported on top)
to **Minecraft 1.12.2, Forge only**.

Differences from the modern branches:

- Single-module Forge mod, no Fabric/Architectury.
- Configuration via the in-game mod list ("Config" button), powered by Forge's
  `@Config` — there are no datapacks in 1.12.2, so aircraft balance lives in
  `config/immersive_aircraft.cfg`.
- No mixins: all hooks use Forge events.

### Building

Requirements: **JDK 8** (ForgeGradle 2.3 does not run on newer JDKs).

```bash
export JAVA_HOME=/path/to/jdk8
./gradlew build
```

The reobfuscated (SRG-named) jar lands in `build/libs/`.

Toolchain: [anatawa12's ForgeGradle 2.3 fork](https://github.com/anatawa12/ForgeGradle-2.3)
(the official ForgeGradle does not work for 1.12.2 on modern JDKs),
Forge 1.12.2-14.23.5.2847, MCP `stable_39`, Gradle 4.9.

### Development runs

```bash
./gradlew runClient
./gradlew runServer
```
