package immersive_aircraft.network.s2c;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.io.Serial;

public class OpenGuiRequest implements Message {
    @Serial
    private static final long serialVersionUID = -2371116419166251497L;

    private final int vehicle;
    private final int syncId;

    public OpenGuiRequest(VehicleEntity vehicle, int syncId) {
        this.vehicle = vehicle.getId();
        this.syncId = syncId;
    }

    @Override
    public void receive(PlayerEntity e) {
        Main.networkManager.handleOpenGuiRequest(this);
    }

    public int getVehicle() {
        return vehicle;
    }

    public int getSyncId() {
        return syncId;
    }
}
