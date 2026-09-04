# Cyberpunk Hovercraft resource/data pack

This optional Minecraft 1.20.1 pack adds two selectable futuristic VTOL skins
to the Immersive Aircraft Cargo Airship. Its resource-pack half supplies the
models and textures; its data-pack half registers those skins and supplies a
six-seat layout plus skin-specific interaction and projectile hitboxes.

![Militech AV and Trauma Team Atlus](preview.png)

Player-scale check in a live Minecraft 1.20.1 client:

![Both aircraft beside the player](in_game_scale.png)

Six occupied seats on each aircraft, counted by the game while airborne:

![Both aircraft carrying six occupants](in_game_six_seats.png)

Live flight check after boarding each aircraft and moving forward/upward:

![Militech AV and Trauma Team Atlus operating in flight](in_game_flight.png)

| Resource-pack vehicle | Immersive Aircraft slot | Flight behavior |
| --- | --- | --- |
| Militech AV | Cargo Airship | 16.32 blocks long; fuel-powered hover/flight, six seats, and cargo storage |
| Trauma Team Atlus | Cargo Airship | 16.32 blocks long; fuel-powered hover/flight, six model-aligned seats, and cargo storage |

The two models are cosmetic choices for the Cargo Airship rather than new
entities. Controls, sounds, inventory, and saved-vehicle behavior come from
the base vehicle. The data pack intentionally disables both
crafting recipes; obtain the aircraft from Creative inventory or with `/give`,
then use the item to place the aircraft in the world.

## Install

1. Build or install the `1.20.1` version of Immersive Aircraft.
2. Zip the contents of this `cyberpunk_hovercraft` directory, or use the
   directory directly.
3. Put the pack in the Minecraft `resourcepacks` directory and enable
   **Militech AV + Trauma Team Atlus** above the default assets.
4. Put the same pack in the world's `datapacks` directory, then run `/reload`.
   This registers the skins, enables all six seats, and adds the full-size
   interaction hitboxes.
5. Take a Cargo Airship from the Creative inventory, then use the item to place
   it in the world. The affected vehicles have no
   crafting recipes while this data pack is enabled.
6. Board the aircraft, press **E**, then click **Vehicle Skins**. Search the
   player's available cosmetics, click one to preview it, and click **Use Skin**
   to apply it.

Both halves are required by the selectable-skin system. If the data pack is not
enabled, its nested skin models remain installed but are not selected.

## Skin access

Skin definitions live in `data/immersive_aircraft/vehicle_skins/`. Trauma Team
Atlus is the free default; Militech AV requires its entitlement tag. For a paid skin, set
`"free": false` and give an entitled player the definition's `unlockTag`, for
example:

```mcfunction
/tag PlayerName add ia.skin.militech_av
```

For development, `/tag PlayerName add ia.skin.all` unlocks every registered
skin. Remove either tag and reopen the vehicle screen to refresh its list.

The server filters the list shown to the player and validates every selection
request. This makes vanilla tags a convenient temporary bridge for CustomNPC;
a payment integration can replace the entitlement provider later without
putting trust in the client. Whenever the controlling driver changes, the
server immediately restores the default if the new driver does not own the
vehicle's currently selected skin.

A skin definition has this shape:

```json
{
  "vehicle": "immersive_aircraft:cargo_airship",
  "model": "immersive_aircraft:vehicle_skins/airship/militech_av",
  "translationKey": "vehicle_skin.immersive_aircraft.militech_av",
  "unlockTag": "ia.skin.militech_av",
  "free": false,
  "default": false,
  "scale": 1.0,
  "seats": [
    {"x": 0.0, "y": 1.0, "z": 1.0}
  ],
  "boundingBoxes": [
    {"width": 4.0, "height": 2.0, "x": 0.0, "y": 1.0, "z": 0.0}
  ]
}
```

`default` skins are always available and are used when a vehicle has no saved
selection. Other skins require either `free: true` or the listed player tag.
The selected ID is synchronized to nearby clients and saved on the placed
vehicle and its picked-up item. The optional flat `seats` list overrides the
vehicle's passenger positions for that model; coordinates are measured in
Minecraft blocks relative to the vehicle origin. The optional positive `scale`
value uniformly scales only the rendered skin; seat coordinates must therefore
already contain their final world-space positions. The optional
`boundingBoxes` list replaces the vehicle's normal interaction and projectile
hitboxes while that skin is active.

Physics, inventory, weapon mounts, and trails still come from the vehicle's
`aircraft/<vehicle>.json`; visual scale, passenger positions, and interaction
hitboxes can vary by skin.

For a local Fabric test, launch `./gradlew :fabric:runClient`, enable this
directory as a resource pack, and also place it in the test world's `datapacks`
directory. After `/reload`, use `/give @s immersive_aircraft:cargo_airship`, board it,
press **E**, and click **Vehicle Skins**. To test entitlement filtering with a
new non-default skin, add
and remove its tag with `/tag @s add <unlockTag>` and
`/tag @s remove <unlockTag>`, reopening the screen after each change.

## Controls and verification

Both skins inherit the Cargo Airship controls: **W/S** moves forward/backward,
**A/D** turns, **Space** rises, **Left Shift** descends, and **R** dismounts.
In a Fabric 1.20.1 client, each item was placed in-world, boarded by right-click,
flown vertically and forward with six occupants, and checked for a passenger
count of six. Both recipe IDs were also confirmed absent after data-pack reload.

## Blockbench and source files

The editable sources are in [`source/`](source/). Both skins use strict
Minecraft-native model specifications and generated 512×512 pixel atlases.
The Militech audit reports 95 cuboids, 19 bones, and 1,140 triangles; the
Trauma Team audit reports 96 cuboids, 21 bones, and 1,152 triangles. Neither
audit reports an error.

The runtime files below `assets/immersive_aircraft/objects/vehicle_skins/` are
native Blockbench `.bbmodel` projects and open directly in Blockbench. Both are
deliberately low-poly cuboid reconstructions with semantic multipart rigs.

Each aircraft is organized as a multipart Blockbench rig rather than one flat
mesh. The outliners expose the hull, cockpit, roof, rear section, interior,
six seats, and four VTOL pods. Both skins' pods tilt by 25 degrees with
forward/reverse input. There are deliberately no generated rotor blades or fan
animations, and the resource pack does not add new key bindings.

The replacement Militech AV is approximately 16.32 × 10.72 × 6.09 blocks,
matching the default Trauma Team Atlus's length. The default Trauma Team Atlus is the
Minecraft-native six-seat ambulance from
`source/trauma_atlus.model-spec.json`. Its 2.75-block authored model is rendered
at 5.93455×, producing a 16.32-block vehicle with six passenger positions
aligned to the two cockpit cushions and four rear-cabin
cushions.

The right-hand renders below use diagnostic colors to show the individual
Blockbench mesh elements; the left-hand renders use the shipped textures.

![Textured aircraft and multipart Blockbench diagnostics](multipart_model.png)

After installing the `img2blockbench` command, regenerate both runtime models
and their atlases with:

```bash
node tools/build_detailed_models.mjs
```

That script validates and compiles `source/militech_av.model-spec.json` and
`source/trauma_atlus.model-spec.json`, then copies each generated model, atlas,
and audit into its runtime location. Skin data makes both models 16.32 blocks
long in the world.

`tools/render_bbmodel.mjs` creates a transparent preview for visual checks and
requires `ffmpeg`:

```bash
node tools/render_bbmodel.mjs \
  --model assets/immersive_aircraft/objects/vehicle_skins/airship/militech_av.bbmodel \
  --texture assets/immersive_aircraft/textures/entity/militech_av.png \
  --output /tmp/militech_av.png
```

Add `--part-colors true` to replace the texture with one diagnostic color per
element. This is useful for checking the semantic split and pod seams. Use
`--pod-tilt -25` to preview the articulated flight pose.

## Asset provenance

Both current models are deterministic `img2blockbench` cuboid reconstructions
based on user-supplied Meshy sources; detailed provenance is stored in their
model specifications. The legacy optimized GLBs remain in `source/` for
reference only. These assets depict third-party fictional designs. No separate
license grant is asserted here; confirm redistribution rights before publishing
or merging them.
