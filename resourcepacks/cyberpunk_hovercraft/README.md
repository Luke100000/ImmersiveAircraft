# Cyberpunk Hovercraft resource/data pack

This optional Minecraft 1.20.1 pack turns two existing Immersive Aircraft
vehicles into large futuristic VTOL aircraft without changing the mod's Java
source or JAR. Its resource-pack half supplies the models and textures; its
data-pack half supplies six-seat layouts and model-sized interaction hitboxes.

![Militech AV and Trauma Team Atlus](preview.png)

Player-scale check in a live Minecraft 1.20.1 client:

![Both aircraft beside the player](in_game_scale.png)

Six occupied seats on each aircraft, counted by the game while airborne:

![Both aircraft carrying six occupants](in_game_six_seats.png)

| Resource-pack vehicle | Immersive Aircraft slot | Flight behavior |
| --- | --- | --- |
| Militech AV | Airship | 8 blocks long; fuel-powered hover/flight, six seats, and one weapon slot |
| Trauma Team Atlus | Cargo Airship | 8 blocks long; fuel-powered hover/flight, six seats, and cargo storage |

Minecraft resource packs cannot register new entity or item IDs. Reusing these
two existing slots is what makes the aircraft work with an unmodified Immersive
Aircraft JAR. Controls, sounds, inventories, and saved-vehicle behavior come
from the respective base vehicles. The data pack intentionally disables both
crafting recipes; obtain the aircraft from Creative inventory or with `/give`,
then use the item to place the aircraft in the world.

## Install

1. Build or install the `1.20.1` version of Immersive Aircraft.
2. Zip the contents of this `cyberpunk_hovercraft` directory, or use the
   directory directly.
3. Put the pack in the Minecraft `resourcepacks` directory and enable
   **Militech AV + Trauma Team Atlus** above the default assets.
4. Put the same pack in the world's `datapacks` directory, then run `/reload`.
   This enables all six seats and the full-size interaction hitboxes.
5. Take an Airship (Militech AV) or Cargo Airship (Trauma Team Atlus) from the
   Creative inventory, then use the item to place it in the world. They have no
   crafting recipes while this data pack is enabled.

The model replacement works with only the resource-pack half enabled, but the
base vehicles remain two-seaters until the same pack's data half is enabled.
No modified mod JAR is required.

## Controls and verification

Both aircraft inherit the Airship controls: **W/S** moves forward/backward,
**A/D** turns, **Space** rises, **Left Shift** descends, and **R** dismounts.
In a Fabric 1.20.1 client, each item was placed in-world, boarded by right-click,
flown vertically and forward with six occupants, and checked for a passenger
count of six. Both recipe IDs were also confirmed absent after data-pack reload.

## Blockbench and source files

The editable sources are in [`source/`](source/). The original Meshy exports
contained roughly 1.96 million triangles per aircraft, which is too dense for
Blockbench and real-time entity rendering. The checked-in GLBs retain the source
UVs and textures but are reduced to about 6,800 triangles and 1024×1024 maps.
They import into Blockbench 5 as Generic Models.

Blockbench does not import glTF in its core editor. Install the community
[glTF Importer](https://github.com/JannisX11/blockbench-plugins/tree/master/plugins/gltf_importer)
from Blockbench's Plugins dialog, then use **File → Import → glTF Model** to
open either `.glb`. The generated `.bbmodel` files do not need that plugin and
open directly.

The runtime files in `assets/immersive_aircraft/objects/` are native Blockbench
`.bbmodel` projects. They can be opened directly in Blockbench. Immersive
Aircraft 1.20.1 accepts only four-vertex mesh faces, so the converter closes each
source triangle with one collinear vertex. That keeps the visible geometry
unchanged while satisfying the loader without a Java patch.

To regenerate a runtime model after editing an optimized GLB, export an
uncompressed GLB with its base-color UVs, save the base-color map as PNG, and
run:

```bash
node tools/glb_to_bbmodel.mjs \
  --input source/militech_av.glb \
  --output assets/immersive_aircraft/objects/airship.bbmodel \
  --texture assets/immersive_aircraft/textures/entity/militech_av.png \
  --texture-name militech_av.png \
  --length-blocks 8 \
  --forward-sign -1 \
  --name body
```

The negative forward sign keeps each cockpit/nose on the vehicle's forward
side and the fins, rear deck, and exhaust points on the trailing side.

`tools/render_bbmodel.mjs` creates a transparent preview for visual checks and
requires `ffmpeg`:

```bash
node tools/render_bbmodel.mjs \
  --model assets/immersive_aircraft/objects/airship.bbmodel \
  --texture assets/immersive_aircraft/textures/entity/militech_av.png \
  --output /tmp/militech_av.png
```

## Asset provenance

The Militech AV and Trauma Team Atlus GLBs were supplied by the contributor for
this pack. They depict third-party fictional designs. No separate license grant
for those two assets is asserted here; confirm redistribution rights before
publishing or merging them.
