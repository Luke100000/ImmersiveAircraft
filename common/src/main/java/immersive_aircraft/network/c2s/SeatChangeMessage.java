package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class SeatChangeMessage extends Message {
    private final int vehicleId;
    private final int seat;

    public SeatChangeMessage(int vehicleId, int seat) {
        this.vehicleId = vehicleId;
        this.seat = seat;
    }

    public SeatChangeMessage(FriendlyByteBuf buffer) {
        vehicleId = buffer.readVarInt();
        seat = buffer.readVarInt();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(vehicleId);
        buffer.writeVarInt(seat);
    }

    @Override
    public void receive(Player player) {
        if (player.getVehicle() instanceof VehicleEntity vehicle && vehicle.getId() == vehicleId) {
            vehicle.changeSeat(player, seat);
        }
    }
}
