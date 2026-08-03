package immersive_aircraft.network;

import immersive_aircraft.network.s2c.InventoryUpdateMessage;

public interface NetworkManager {
    void handleInventoryUpdate(InventoryUpdateMessage message);

    void handleFire(immersive_aircraft.network.s2c.FireResponse response);
}
