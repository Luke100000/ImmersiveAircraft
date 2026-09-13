package immersive_aircraft;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.network.c2s.*;
import immersive_aircraft.network.s2c.*;

public class Messages {
    public static void loadMessages() {
        NetworkHandler.registerClientbound("aircraft_data", AircraftDataMessage.class, AircraftDataMessage::new);
        NetworkHandler.registerClientbound("fire_response", FireResponse.class, FireResponse::new);
        NetworkHandler.registerClientbound("inventory_update", InventoryUpdateMessage.class, InventoryUpdateMessage::new);
        NetworkHandler.registerClientbound("open_gui", OpenGuiRequest.class, OpenGuiRequest::new);
        NetworkHandler.registerClientbound("vehicle_upgrades", VehicleUpgradesMessage.class, VehicleUpgradesMessage::new);

        NetworkHandler.registerServerbound("collision", CollisionMessage.class, CollisionMessage::new);
        NetworkHandler.registerServerbound("command", CommandMessage.class, CommandMessage::new);
        NetworkHandler.registerServerbound("engine_power", EnginePowerMessage.class, EnginePowerMessage::new);
        NetworkHandler.registerServerbound("fire", FireMessage.class, FireMessage::new);
        NetworkHandler.registerServerbound("inventory", RequestInventory.class, RequestInventory::new);
    }
}
