package immersive_aircraft.network.s2c;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class OpenGuiRequest extends Message {
    private final int vehicle;
    private final int syncId;

    public OpenGuiRequest(VehicleEntity vehicle, int syncId) {
        this.vehicle = vehicle.getId();
        this.syncId = syncId;
    }

    public OpenGuiRequest(PacketByteBuf b) {
        vehicle = b.readInt();
        syncId = b.readInt();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeInt(vehicle);
        b.writeInt(syncId);
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
