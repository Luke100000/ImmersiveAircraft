package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class CollisionMessage extends Message {
    private final float damage;

    public CollisionMessage(float damage) {
        this.damage = damage;
    }

    public CollisionMessage(PacketByteBuf b) {
        damage = b.readFloat();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeFloat(damage);
    }

    @Override
    public void receive(PlayerEntity e) {
        if (e.getRootVehicle() instanceof VehicleEntity vehicle) {
            vehicle.damage(DamageSource.FALL, damage);
            if (vehicle.isRemoved()) {
                float crashDamage = damage * Config.getInstance().crashDamage;
                if (Config.getInstance().preventKillThroughCrash) {
                    crashDamage = Math.min(crashDamage, e.getHealth() - 1.0f);
                }
                e.damage(DamageSource.FALL, crashDamage);
            }
        }
    }
}
