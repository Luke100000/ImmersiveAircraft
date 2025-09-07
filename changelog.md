# 1.4.0

* Added mechanical dials and hud (Thanks donmor!)
* Fixed keybind related issues

# 1.3.3

* Fixed rudder orientation

# 1.3.2

* Fixed aircraft sinking on Forge
* Fixed warship position offset

# 1.3.1

* Fixed recipe

# 1.3.0

* Added the Bamboo Hopper
* Made trails data driven
* Fixed a crash

# 1.2.4

* Fixed Fabric networking being a bit funky

# 1.2.2

* Updated API-related stuff

# 1.2.1

* Fixed air bubbles and other bars being offset

# 1.2.0

* Fixed some crashes
* Fixed health bar position conflicts on Forge (thanks sfiomn!)
* Added config flags
    * `dropAircraft` to make aircraft non-pickup-able
    * `rotaryCannonDamage`
    * `heavyCrossBowVelocity`
    * `bombBayEntity` A mapping from ammo item to spawned entity

# 1.1.8

* Maybe fixed concurrent addon loading issues on Forge
* Maybe fixed multiplayer issues with turning of engines

# 1.1.7

* A dummy update to fix a mess-up with file uploads

# 1.1.6

* Sync with 1.20.1 and relaxed versions to include 1.21

# 1.1.5

* Fixed crash on NeoForge
* Fixed Banners not rendering correctly

# 1.1.4

* Fixed recipes in 1.21.1

# 1.1.3

* Added damage multiplier in config
* Ported to 1.21.1

# 1.1.1, 1.1.2

* Forge/Fabric will now complain when an unsupported version of ad astra is used

# 1.1.0

* Fixed inventory sometimes desyncing
* Fixed fuel levels not being saved
* Arrows shot by the heavy crossbow are no longer picked up by the player as this caused duplication
* Anvil names are now treated properly (thanks Cibernet!)
* Added colorable aircraft (thanks Cibernet!)
    * Mostly interesting for addons with models designed for it
    * Added support for Airship, Cargo Airship, and the Warship
    * Coloring works like leather (combine with dye in crafting, clear with cauldron)
* Placing a vehicle invalidly now prints an error message
* Fixed banners sometimes not rendered correctly
* Added compat with Destroy
* Added Ad Astra Compat (Thanks Erdragh!)
* You can now turn on vehicle health generation in the config (`regenerateHealthEveryNTicks`)
    * This brings it closer to the vanilla boat mechanic while still having health rather than "wobbliness"
    * With the second new flag `requireShiftForRepair` you can also require shift to repair the vehicle
* Added configurable `repairExhaustion` (small hunger cost for repairing)
* You can now destroy aircraft much quicker with your hand, when they are empty

# 1.0.2

* Internal changes for addons

# 1.0.1

* Fixed infinite fuel airship
* Fixed wrong keybinds on Forge
* Fixed offset weapons
* Increased culling box of warship so avoid popping in and out

# 1.0.0

* Added the Warship (thanks Maugwei!)
* Changes to core, keep addons up to date!
    * Made many changes to allow easier addons
* Vehicles now implement inventory interface, thus working with hoppers etc
* Rockets now boost based on duration
* You no longer yeet yourself out of the aircraft when sneak-right clicking
* No more mining slowdown while in aircraft
* Added config flags to:
    * Drop the inventory (default true to stay faithful to e.g., boats)
    * Drop the equipment/customization (default false, as that's just annoying)

# 0.7.5

* Various smaller fixes
* Added config flag to allow block damage for bomb bay
* Fixed heavy crossbow in creative
* Durability upgrades no longer mess with vehicle health bar

# 0.7.4

* Fixed issues with building
* Fixed background not being dim in inventory

# 0.7.3

* Added camera offset, especially for larger aircraft
* Fixed cargo airship's sails
* You can now exit every aircraft properly, and don't stand on its top

# 0.7.2

* Fixed negative modifiers not working as intended
* Added rotary cannon sound
* Creative players no longer require ammo
* The gyroscope now also stabilizes the pitch
* Updated icons (Thanks 김작업!)
* Weapons now also work when daylight cycle is off
* Fixed bullets being funky
* Added fancy sinking bubbles

# 0.7.1

* TNT dropper now requires 0.2 TNT per shot, instead of 0.02 typo
* Added keybind for using weapons/mounts
* Fixed gunpowder typo, can now be used in rotary cannon
* Added a cooldown, so you don't bomb your aircraft when entering
* Fixed sound of telescope
* Fixed client ticking at higher rate than server

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