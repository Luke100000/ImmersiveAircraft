package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.entity.player.PlayerEntity;

public class CollisionMessage implements Message {
    private final float damage;

    public CollisionMessage(float damage) {
        this.damage = damage;
    }

    @Override
    public void receive(PlayerEntity e) {
        if (e.getRootVehicle() instanceof VehicleEntity vehicle) {
            vehicle.damage(e.world.getDamageSources().fall(), damage);
        }
    }
}
