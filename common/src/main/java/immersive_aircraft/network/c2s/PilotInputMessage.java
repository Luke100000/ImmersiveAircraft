package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PilotInputMessage extends Message {
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

    public PilotInputMessage(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(vehicleId);
        buffer.writeFloat(x);
        buffer.writeFloat(y);
        buffer.writeFloat(z);
    }

    @Override
    public void receive(Player player) {
        if (player instanceof ServerPlayer serverPlayer && player.getVehicle() instanceof VehicleEntity vehicle && vehicle.getId() == vehicleId) {
            vehicle.receivePilotInput(serverPlayer, x, y, z);
        }
    }
}
