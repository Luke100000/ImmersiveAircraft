package immersive_aircraft.entity.bullet;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class BulletEntity extends ThrowableItemProjectile {
    public BulletEntity(EntityType<? extends BulletEntity> entityType, Level level) {
        super(entityType, level);
    }

    protected Item getDefaultItem() {
        return Items.IRON_NUGGET;
    }

    private ParticleOptions getParticle() {
        return ParticleTypes.EXPLOSION;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ParticleOptions particleOptions = this.getParticle();
            for (int i = 0; i < 5; ++i) {
                this.level.addParticle(particleOptions, this.getX(), this.getY(), this.getZ(), 0.0f, 0.0f, 0.0f);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (canHitEntity(result.getEntity())) {
            float damage = 5.0f;
            result.getEntity().hurt(DamageSource.thrown(this, this.getOwner()), damage);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level.isClientSide) {
            if (result instanceof EntityHitResult entityHitResult && !canHitEntity(entityHitResult.getEntity())) {
                return;
            }
            this.level.broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return getOwner() != target && target.getVehicle() != getOwner() && super.canHitEntity(target);
    }
}
