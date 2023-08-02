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

# 0.2.1

* Fixed Forge on 1.19.x

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