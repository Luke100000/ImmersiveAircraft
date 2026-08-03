package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.InventoryVehicleEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3d;

public class FireMessage extends Message {
    private int slot;
    private int index;
    public Vec3d direction;

    public FireMessage() {
    }

    public FireMessage(int slot, int index, Vec3d direction) {
        this.slot = slot;
        this.index = index;
        this.direction = direction;
    }

    public int getSlot() {
        return slot;
    }

    public int getIndex() {
        return index;
    }

    @Override
    protected void decode(PacketBuffer b) {
        slot = b.readInt();
        index = b.readInt();
        direction = new Vec3d(b.readFloat(), b.readFloat(), b.readFloat());
    }

    @Override
    public void encode(PacketBuffer b) {
        b.writeInt(slot);
        b.writeInt(index);
        b.writeFloat((float) direction.x);
        b.writeFloat((float) direction.y);
        b.writeFloat((float) direction.z);
    }

    @Override
    public void receive(EntityPlayer e) {
        if (e.getRidingEntity() instanceof InventoryVehicleEntity) {
            ((InventoryVehicleEntity) e.getRidingEntity()).fireWeapon(slot, index, direction);
        }
    }
}
