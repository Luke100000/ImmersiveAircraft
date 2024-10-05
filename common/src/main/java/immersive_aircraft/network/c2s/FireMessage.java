package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.InventoryVehicleEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

public class FireMessage extends Message {
    public static final StreamCodec<RegistryFriendlyByteBuf, FireMessage> STREAM_CODEC = StreamCodec.ofMember(FireMessage::encode, FireMessage::new);
    public static final CustomPacketPayload.Type<FireMessage> TYPE = Message.createType("fire");

    public CustomPacketPayload.Type<FireMessage> type() {
        return TYPE;
    }

    private final int slot;
    private final int index;
    public final Vector3f direction;

    public FireMessage(int slot, int index, Vector3f direction) {
        this.slot = slot;
        this.index = index;
        this.direction = direction;
    }

    public FireMessage(RegistryFriendlyByteBuf b) {
        slot = b.readInt();
        index = b.readInt();
        direction = new Vector3f(b.readFloat(), b.readFloat(), b.readFloat());
    }

    public int getSlot() {
        return slot;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf b) {
        b.writeInt(slot);
        b.writeInt(index);
        b.writeFloat(direction.x());
        b.writeFloat(direction.y());
        b.writeFloat(direction.z());
    }

    @Override
    public void receiveServer(ServerPlayer e) {
        if (e.getVehicle() instanceof InventoryVehicleEntity vehicle) {
            vehicle.fireWeapon(slot, index, direction);
        }
    }
}
