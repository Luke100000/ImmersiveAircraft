package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;

public class CollisionMessage extends Message {
    private float damage;

    public CollisionMessage() {
    }

    public CollisionMessage(float damage) {
        this.damage = damage;
    }

    @Override
    protected void decode(PacketBuffer b) {
        damage = b.readFloat();
    }

    @Override
    public void encode(PacketBuffer b) {
        b.writeFloat(damage);
    }

    @Override
    public void receive(EntityPlayer e) {
        if (e.getLowestRidingEntity() instanceof VehicleEntity) {
            VehicleEntity vehicle = (VehicleEntity) e.getLowestRidingEntity();
            vehicle.attackEntityFrom(DamageSource.FALL, damage);
            if (vehicle.isDead) {
                float crashDamage = damage * Config.getInstance().crashDamage;
                if (Config.getInstance().preventKillThroughCrash) {
                    crashDamage = Math.min(crashDamage, e.getHealth() - 1.0f);
                }
                e.attackEntityFrom(DamageSource.FALL, crashDamage);
            }
        }
    }
}
