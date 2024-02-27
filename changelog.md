# 0.7.2

* Fixed negative modifiers not working as intended

# 0.7.1

* TNT dropper now requires 0.2 TNT per shot, instead of 0.02 typo
* Added keybind for using weapons/mounts
* Fixed gunpowder typo, can now be used in rotary cannon
* Added a cooldown, so you don't bomb your aircraft when entering
* Fixed sound of telescope

# 0.7.0

* Aircraft now have a health bar, building up visual damage until they explode
    * Right click fixes it again
* Added 4 weapon/utilities
    * Telescope – Twice the zoom as a spyglass and always available
    * Heavy Crossbow – Shots arrows with a lot of force
    * Rotary Cannon – Automatic cannon on a gyroscopic mount
    * Bomb Bay – Drops tiny TNT packets (no block destruction)
* Reorders slots to no longer burn your banners (Existing aircraft needs some manual fixing)
* Added support for modded fuel (Thanks Brandon!)
* Biplanes can now reverse
* REI is now also supported
* Fixed client tracking range (Vehicles are now 2.4x farther visible)
* You now have to confirm R to dismount when in flight
* You now get reminded how to exit when trying to shift
* Quadrocopter now have a strafing movement and do not affect the camera, making it way more usable for building and
  first person operations
* Aircraft are now party data driven, allowing easier modification of model, inventory, weapon mounts etc

# 0.6.2

* Fixed another server crash on Fabric
* Reverted removal of Multi-Keybindings as player got angry

# 0.6.1

* Fixed server crash on Fabric

# 0.6.0

* Added datapack support for custom upgrades
* Configurable vehicle explosions
* Removed Multi-Keybindings as it no longer works with Forge anyways
    * Also removed separate keybinds for pull and push
* Added config to block aircraft in certain dimensions
* Added JEI support for the larger inventories

# 0.5.2

* Switched to PacketByteBuf serializer

# 0.5.1

* Fixed glitch on sails when pausing
* Aircraft no longer break on autostep

# 0.5.0

* Added Cargo Airship
* You can now kick out entities using shift-right click
* Added fall damage when crashing the aircraft (never lethal by default, configurable)
* Fixed inventory staying open after entity got removed
* Mirrored banners correctly

# 0.4.2

* Engines now have a fuel buffer and then die slowly when out of fuel
* Configurable fuel consumption can now exceed 1
* If fuel consumption is set to 0, no initial item needs to get burned
* Don't burn fuel in creative mode (configurable)
* The engine no longer burns the whole bucket
* Added a configurable fuel map
* Cut default wind sensibility in half
* Changed back to custom Keybindings, allowing to reuse keys without conflicts
    * A config flag exists to disable this in case of mod conflicts

# 0.4.1

* Fixed fuel notification on gyrodyne
* Fixed air friction stat color
* Fixed server crash

# 0.4.0

* Added inventory
* Added fuel
    * The Gyrodyne burns your body fat instead
* Added 9 unique upgrade items
* Added banners to enhance the look of the Airplane and Airship
* Added dye slots to tint the Airships sails
* Added rocket boost
* Improved wind mechanics, configurable
* Crashing aircraft now destroys them, configurable
    * By default, only player can destroy aircraft to avoid a rogue skeleton throwing your stuff on the floor

# 0.3.1

* Used even less aggressive keybinding method
* Fixed discriminator byte crashes

# 0.3.0

* Added Quadrocopter
* Fixed non-pilot dismounting
* Switched to less intrusive multi keybindings injection to restore mod compatibility
* Fixed controller animation of airship
* Added support for modmenu and cloth config
* Added item tooltip description
* You can no longer apply knockback to your vehicle
* Fixed the Biplane phantom push when leaving
* Fixed first person camera offset
* Vehicles can have a separate camera perspective, and is third person by default (configurable)
* Creative mode destroys airplanes immediately

# 0.2.0

* Added keybindings
* Added fancy GUI
* Improved and optimized aircraft textures (thanks stohun!)
* Fixed and updated translations
* Fixed WAILA translations
* Enhanced recipes
* Fixed non player mount positions
* Made all vehicles more durable
* You can no longer hit your own aircraft while flying
* Random entity will never occupy the last seat

# 0.1.1

* Fixed broken sneaking animation outside aircraft

# 0.1.0

* Initial release
* Added hull, engine, sail, propeller and boiler item
* Added airship, biplane and gyrodyne aircraft