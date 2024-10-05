package immersive_aircraft.network;

import immersive_aircraft.network.s2c.FireResponse;
import immersive_aircraft.network.s2c.InventoryUpdateMessage;
import immersive_aircraft.network.s2c.OpenGuiRequest;

public interface MessageHandler {
    void handleOpenGuiRequest(OpenGuiRequest request);

    void handleInventoryUpdate(InventoryUpdateMessage message);

    void handleFire(FireResponse fireResponse);
}
