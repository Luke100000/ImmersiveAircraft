package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class CommandMessage extends Message {
    public static final StreamCodec<RegistryFriendlyByteBuf, CommandMessage> STREAM_CODEC = StreamCodec.ofMember(CommandMessage::encode, CommandMessage::new);
    public static final CustomPacketPayload.Type<CommandMessage> TYPE = Message.createType("command");

    public CustomPacketPayload.Type<CommandMessage> type() {
        return TYPE;
    }

    private final Key key;
    private final double fx;
    private final double fy;
    private final double fz;

    public CommandMessage(Key key, Vec3 velocity) {
        this.key = key;
        this.fx = velocity.x;
        this.fy = velocity.y;
        this.fz = velocity.z;
    }

    public CommandMessage(RegistryFriendlyByteBuf b) {
        key = Key.values()[b.readInt()];
        fx = b.readDouble();
        fy = b.readDouble();
        fz = b.readDouble();
    }

    @Override
    public void encode(RegistryFriendlyByteBuf b) {
        b.writeInt(key.ordinal());
        b.writeDouble(fx);
        b.writeDouble(fy);
        b.writeDouble(fz);
    }

    @Override
    public void receiveServer(ServerPlayer e) {
        if (e.getRootVehicle() instanceof VehicleEntity vehicle) {
            if (key == Key.DISMOUNT) {
                e.stopRiding();
                e.setJumping(false);
                vehicle.chill();
                vehicle.setDeltaMovement(fx, fy, fz);
            } else if (key == Key.BOOST) {
                if (vehicle.canBoost()) {
                    vehicle.boost();
                }
            }
        }
    }

    public enum Key {
        DISMOUNT,
        BOOST,
        DAMAGE
    }
}
