package immersive_aircraft.entity.bullet;

import immersive_aircraft.config.Config;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class TinyTNT extends PrimedTnt {
    private Entity owner;

    public TinyTNT(EntityType<? extends PrimedTnt> entityType, Level level) {
        super(entityType, level);
    }

    public void setOwner(Entity owner) {
        this.owner = owner;
    }

    @Override
    public void tick() {
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
        }
        int i = this.getFuse() - (onGround() ? 5 : 1);
        this.setFuse(i);
        if (i <= 0) {
            this.discard();
            if (!this.level().isClientSide()) {
                this.boom();
            }
        } else {
            this.updateInWaterStateAndDoFluidPushing();
            if (this.level().isClientSide()) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    private void boom() {
        // Визуальный взрыв (без урона сущностям и блокам)
        this.level().explode(this, this.getX(), this.getY(0.0625), this.getZ(), 4.0f, Level.ExplosionInteraction.NONE);

        if (Config.getInstance().weaponsAreDestructive) {
            // Ручной расчёт урона, исключая владельца (свой самолёт)
            float radius = 4.0f;
            AABB aabb = new AABB(
                    this.getX() - radius, this.getY() - radius, this.getZ() - radius,
                    this.getX() + radius, this.getY() + radius, this.getZ() + radius
            );
            for (Entity entity : this.level().getEntities(this, aabb)) {
                if (entity == owner || !entity.isAlive() || !entity.isPickable()) {
                    continue;
                }
                double distance = Math.sqrt(this.distanceToSqr(entity));
                if (distance < radius) {
                    float damage = (float) ((radius - distance) / radius * 20.0);
                    entity.hurt(this.level().damageSources().explosion(this, owner), damage);
                }
            }
        }
    }
}
