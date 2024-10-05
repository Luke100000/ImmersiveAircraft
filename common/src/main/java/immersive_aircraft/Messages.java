package immersive_aircraft;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.network.c2s.*;
import immersive_aircraft.network.s2c.*;

public class Messages {
    public static void loadMessages() {
        NetworkHandler.registerMessage(AircraftDataMessage.TYPE, AircraftDataMessage.STREAM_CODEC);
        NetworkHandler.registerMessage(FireResponse.TYPE, FireResponse.STREAM_CODEC);
        NetworkHandler.registerMessage(InventoryUpdateMessage.TYPE, InventoryUpdateMessage.STREAM_CODEC);
        NetworkHandler.registerMessage(OpenGuiRequest.TYPE, OpenGuiRequest.STREAM_CODEC);
        NetworkHandler.registerMessage(VehicleUpgradesMessage.TYPE, VehicleUpgradesMessage.STREAM_CODEC);

        NetworkHandler.registerMessage(CollisionMessage.TYPE, CollisionMessage.STREAM_CODEC);
        NetworkHandler.registerMessage(CommandMessage.TYPE, CommandMessage.STREAM_CODEC);
        NetworkHandler.registerMessage(EnginePowerMessage.TYPE, EnginePowerMessage.STREAM_CODEC);
        NetworkHandler.registerMessage(FireMessage.TYPE, FireMessage.STREAM_CODEC);
        NetworkHandler.registerMessage(InventoryRequest.TYPE, InventoryRequest.STREAM_CODEC);
    }
}
