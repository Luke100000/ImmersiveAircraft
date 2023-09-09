package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class CommandMessage extends Message {
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

    public CommandMessage(FriendlyByteBuf b) {
        key = Key.values()[b.readInt()];
        fx = b.readDouble();
        fy = b.readDouble();
        fz = b.readDouble();
    }

    @Override
    public void encode(FriendlyByteBuf b) {
        b.writeInt(key.ordinal());
        b.writeDouble(fx);
        b.writeDouble(fy);
        b.writeDouble(fz);
    }

    @Override
    public void receive(Player e) {
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

        if (e.getRootVehicle() instanceof InventoryVehicleEntity vehicle) {
            if (key == Key.INVENTORY) {
                vehicle.openInventory((ServerPlayer) e);
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
