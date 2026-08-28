# Skyline Aerodyne

`skyline_aerodyne.bbmodel` is a hand-authored, original Blockbench model integrated as a new Immersive Aircraft vehicle. It uses the existing quadrocopter flight mechanics and inventory layout, with a powered/off engine-light animation controlled by `SkylineAerodyneEntityRenderer`.

It is intentionally an original design: the catalogue work against the local Cyberpunk installation is reference research only. No REDengine mesh, texture, logo, material, or game file is included here.

Open `common/src/main/resources/assets/immersive_aircraft/objects/skyline_aerodyne.bbmodel` directly in Blockbench. Keep the `engine`, `engine_0`, and `engine_1` names when editing, as the renderer uses them for the powered visual state.
