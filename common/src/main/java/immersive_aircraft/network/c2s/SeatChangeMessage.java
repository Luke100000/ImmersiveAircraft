package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class SeatChangeMessage extends Message {
    public static final StreamCodec<RegistryFriendlyByteBuf, SeatChangeMessage> STREAM_CODEC = StreamCodec.ofMember(SeatChangeMessage::encode, SeatChangeMessage::new);
    public static final CustomPacketPayload.Type<SeatChangeMessage> TYPE = Message.createType("change_seat");

    private final int vehicleId;
    private final int seat;

    public SeatChangeMessage(int vehicleId, int seat) {
        this.vehicleId = vehicleId;
        this.seat = seat;
    }

    public SeatChangeMessage(RegistryFriendlyByteBuf buffer) {
        vehicleId = buffer.readVarInt();
        seat = buffer.readVarInt();
    }

    @Override
    public CustomPacketPayload.Type<SeatChangeMessage> type() {
        return TYPE;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(vehicleId);
        buffer.writeVarInt(seat);
    }

    @Override
    public void receiveServer(ServerPlayer player) {
        if (player.getVehicle() instanceof VehicleEntity vehicle && vehicle.getId() == vehicleId) {
            vehicle.changeSeat(player, seat);
        }
    }
}
