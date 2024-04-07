package immersive_aircraft;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.network.c2s.*;
import immersive_aircraft.network.s2c.*;

public class Messages {
    public static void loadMessages() {
        NetworkHandler.registerMessage(EnginePowerMessage.class, EnginePowerMessage::new);
        NetworkHandler.registerMessage(CommandMessage.class, CommandMessage::new);
        NetworkHandler.registerMessage(OpenGuiRequest.class, OpenGuiRequest::new);
        NetworkHandler.registerMessage(InventoryUpdateMessage.class, InventoryUpdateMessage::new);
        NetworkHandler.registerMessage(RequestInventory.class, RequestInventory::new);
        NetworkHandler.registerMessage(CollisionMessage.class, CollisionMessage::new);
        NetworkHandler.registerMessage(VehicleUpgradesMessage.class, VehicleUpgradesMessage::new);
        NetworkHandler.registerMessage(AircraftDataMessage.class, AircraftDataMessage::new);
        NetworkHandler.registerMessage(FireMessage.class, FireMessage::new);
        NetworkHandler.registerMessage(FireResponse.class, FireResponse::new);
    }
}
