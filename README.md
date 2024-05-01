# Immersive Aircraft

This mod adds bunch of rustic aircraft to travel, transport, and explore! The aircraft have a strong focus on being
vanilla-faithful and many details and functionalities, without being overly complicated.

[![Crowdin](https://badges.crowdin.net/immersive-collection/localized.svg)](https://crowdin.com/project/immersive-collection)

Hosted on
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/immersive-aircraft) and
[Modrinth](https://modrinth.com/mod/immersive-aircraft)

# Contributors

* Favouriteless (Added datapack support and exploding vehicle config)
* stohun (Reworked entity textures)
* 김작업 (Reworked icon textures)
* Everyone who helped [to translate](https://crowdin.com/project/immersive-collection)

# Addons

Many helpful registries and generic functions are available to quickstart an addon.

* `InventoryVehicleEntity` provides an abstract vehicle with inventory and datapack configuration.
* `VehicleStat` provides a way to add custom stats to vehicles.
* `VehicleInventoryDescription` provides a way to register custom slots.
* `SlotRenderer` provides a way to render custom slots.
* `JsonConfig` can be extended to have an own config options.
* `NetworkHandler` and `Registration` can be used instead of e.g., Architectury to stay launcher independent.

Check out existing addons for references:

* [Man of Many Planes](https://github.com/Luke100000/Man-of-Many-Planes)
* [Immersive Machinery](https://github.com/Luke100000/ImmersiveMachinery)