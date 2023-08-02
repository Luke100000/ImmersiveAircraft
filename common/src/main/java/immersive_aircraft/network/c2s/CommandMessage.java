package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class CommandMessage extends Message {
    private final Key key;
    private final double fx;
    private final double fy;
    private final double fz;

    public CommandMessage(Key key, Vec3d velocity) {
        this.key = key;
        this.fx = velocity.x;
        this.fy = velocity.y;
        this.fz = velocity.z;
    }

    public CommandMessage(PacketByteBuf b) {
        key = Key.values()[b.readInt()];
        fx = b.readDouble();
        fy = b.readDouble();
        fz = b.readDouble();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeInt(key.ordinal());
        b.writeDouble(fx);
        b.writeDouble(fy);
        b.writeDouble(fz);
    }

    @Override
    public void receive(PlayerEntity e) {
        if (e.getRootVehicle() instanceof VehicleEntity) {
            VehicleEntity vehicle = (VehicleEntity) e.getRootVehicle();
            if (key == Key.DISMOUNT) {
                e.stopRiding();
                vehicle.chill();
                vehicle.setVelocity(fx, fy, fz);
            } else if (key == Key.BOOST) {
                if (vehicle.canBoost()) {
                    vehicle.boost();
                }
            }
        }

        if (e.getRootVehicle() instanceof InventoryVehicleEntity) {
            InventoryVehicleEntity vehicle = (InventoryVehicleEntity) e.getRootVehicle();
            if (key == Key.INVENTORY) {
                vehicle.openInventory((ServerPlayerEntity)e);
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
