package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3d;

public class CommandMessage extends Message {
    private Key key;
    private double fx;
    private double fy;
    private double fz;

    public CommandMessage() {
    }

    public CommandMessage(Key key, Vec3d velocity) {
        this.key = key;
        this.fx = velocity.x;
        this.fy = velocity.y;
        this.fz = velocity.z;
    }

    @Override
    protected void decode(PacketBuffer b) {
        key = Key.values()[b.readInt()];
        fx = b.readDouble();
        fy = b.readDouble();
        fz = b.readDouble();
    }

    @Override
    public void encode(PacketBuffer b) {
        b.writeInt(key.ordinal());
        b.writeDouble(fx);
        b.writeDouble(fy);
        b.writeDouble(fz);
    }

    @Override
    public void receive(EntityPlayer e) {
        if (e.getLowestRidingEntity() instanceof VehicleEntity) {
            VehicleEntity vehicle = (VehicleEntity) e.getLowestRidingEntity();
            if (key == Key.DISMOUNT) {
                e.dismountRidingEntity();
                vehicle.chill();
                vehicle.setVelocity(fx, fy, fz);
            } else if (key == Key.BOOST) {
                if (vehicle.canBoost()) {
                    vehicle.boost();
                }
            }
        }

        if (e.getLowestRidingEntity() instanceof InventoryVehicleEntity) {
            InventoryVehicleEntity vehicle = (InventoryVehicleEntity) e.getLowestRidingEntity();
            if (key == Key.INVENTORY) {
                vehicle.openInventory((EntityPlayerMP)e);
            }
        }
    }

    public enum Key {
        DISMOUNT,
        INVENTORY,
        BOOST,
        DAMAGE
    }
}
