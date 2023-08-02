package immersive_aircraft;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.network.c2s.CollisionMessage;
import immersive_aircraft.network.c2s.CommandMessage;
import immersive_aircraft.network.c2s.EnginePowerMessage;
import immersive_aircraft.network.c2s.RequestInventory;
import immersive_aircraft.network.s2c.InventoryUpdateMessage;
import immersive_aircraft.network.s2c.OpenGuiRequest;

public class Messages {
    public static void loadMessages() {
        NetworkHandler.registerMessage(EnginePowerMessage.class, EnginePowerMessage::new);
        NetworkHandler.registerMessage(CommandMessage.class, CommandMessage::new);
        NetworkHandler.registerMessage(OpenGuiRequest.class, OpenGuiRequest::new);
        NetworkHandler.registerMessage(InventoryUpdateMessage.class, InventoryUpdateMessage::new);
        NetworkHandler.registerMessage(RequestInventory.class, RequestInventory::new);
        NetworkHandler.registerMessage(CollisionMessage.class, CollisionMessage::new);
    }
}
