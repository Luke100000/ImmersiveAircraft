# Skyline Aerodyne

`skyline_aerodyne.bbmodel` is an original Blockbench model integrated as a new Immersive Aircraft vehicle. It uses the existing quadrocopter flight mechanics and inventory layout.

The editable source is `common/src/main/resources/assets/immersive_aircraft/objects/skyline_aerodyne.img2blockbench.json`. It was compiled with Img2Blockbench from the original construction sheet in `objects/reference/skyline_aerodyne_reference.png`. The strict audit reports 10 bones, 36 semantic cuboids, a 512×512 pixel atlas, and no errors or warnings.

It is intentionally an original design: the catalogue work against the local Cyberpunk installation is reference research only. No REDengine mesh, texture, logo, material, or game file is included here.

Open `common/src/main/resources/assets/immersive_aircraft/objects/skyline_aerodyne.bbmodel` directly in Blockbench. Make geometry changes in the Img2Blockbench JSON source, run its strict validation and build commands, then replace the generated `.bbmodel` and entity texture together.
