package immersive_aircraft.network.c2s;

import com.mojang.math.Vector3f;
import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.weapons.Weapon;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class FireMessage extends Message {
    private final int slot;
    public final Vector3f direction;

    public FireMessage(int slot, Vector3f direction) {
        this.slot = slot;
        this.direction = direction;
    }

    public FireMessage(FriendlyByteBuf b) {
        slot = b.readInt();
        direction = new Vector3f(b.readFloat(), b.readFloat(), b.readFloat());
    }

    public int getSlot() {
        return slot;
    }

    @Override
    public void encode(FriendlyByteBuf b) {
        b.writeInt(slot);
        b.writeFloat(direction.x());
        b.writeFloat(direction.y());
        b.writeFloat(direction.z());
    }

    @Override
    public void receive(Player e) {
        if (e.getVehicle() instanceof InventoryVehicleEntity vehicle) {
            for (Weapon weapon : vehicle.getWeapons().get(slot)) {
                weapon.fire(direction);
            }
        }
    }
}
