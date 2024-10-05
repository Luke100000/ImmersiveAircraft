package immersive_aircraft.network.s2c;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public class OpenGuiRequest extends Message {
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenGuiRequest> STREAM_CODEC = StreamCodec.ofMember(OpenGuiRequest::encode, OpenGuiRequest::new);
    public static final CustomPacketPayload.Type<OpenGuiRequest> TYPE = Message.createType("open_gui");

    public CustomPacketPayload.Type<OpenGuiRequest> type() {
        return TYPE;
    }

    private final int vehicle;
    private final int syncId;

    public OpenGuiRequest(VehicleEntity vehicle, int syncId) {
        this.vehicle = vehicle.getId();
        this.syncId = syncId;
    }

    public OpenGuiRequest(RegistryFriendlyByteBuf b) {
        vehicle = b.readInt();
        syncId = b.readInt();
    }

    @Override
    public void encode(RegistryFriendlyByteBuf b) {
        b.writeInt(vehicle);
        b.writeInt(syncId);
    }

    @Override
    public void receiveClient() {
        Main.messageHandler.handleOpenGuiRequest(this);
    }

    public int getVehicle() {
        return vehicle;
    }

    public int getSyncId() {
        return syncId;
    }
}
