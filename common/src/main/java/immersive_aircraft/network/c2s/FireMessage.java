package immersive_aircraft.network.c2s;

import org.joml.Vector3f;
import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.InventoryVehicleEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class FireMessage extends Message {
    private final int slot;
    private final int index;
    public final Vector3f direction;

    public FireMessage(int slot, int index, Vector3f direction) {
        this.slot = slot;
        this.index = index;
        this.direction = direction;
    }

    public FireMessage(FriendlyByteBuf b) {
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
    public void encode(FriendlyByteBuf b) {
        b.writeInt(slot);
        b.writeInt(index);
        b.writeFloat(direction.x());
        b.writeFloat(direction.y());
        b.writeFloat(direction.z());
    }

    @Override
    public void receive(Player e) {
        if (e.getVehicle() instanceof InventoryVehicleEntity vehicle) {
            vehicle.fireWeapon(slot, index, direction);
        }
    }
}
