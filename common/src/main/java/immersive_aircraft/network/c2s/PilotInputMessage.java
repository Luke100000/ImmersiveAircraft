package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class PilotInputMessage extends Message {
    public static final StreamCodec<RegistryFriendlyByteBuf, PilotInputMessage> STREAM_CODEC = StreamCodec.ofMember(PilotInputMessage::encode, PilotInputMessage::new);
    public static final CustomPacketPayload.Type<PilotInputMessage> TYPE = Message.createType("pilot_input");

    private final int vehicleId;
    private final float x;
    private final float y;
    private final float z;

    public PilotInputMessage(int vehicleId, float x, float y, float z) {
        this.vehicleId = vehicleId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PilotInputMessage(RegistryFriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    @Override
    public CustomPacketPayload.Type<PilotInputMessage> type() {
        return TYPE;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(vehicleId);
        buffer.writeFloat(x);
        buffer.writeFloat(y);
        buffer.writeFloat(z);
    }

    @Override
    public void receiveServer(ServerPlayer player) {
        if (player.getVehicle() instanceof VehicleEntity vehicle && vehicle.getId() == vehicleId) {
            vehicle.receivePilotInput(player, x, y, z);
        }
    }
}
