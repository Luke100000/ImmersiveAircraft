package immersive_aircraft.entity.bullet;

import immersive_aircraft.config.Config;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;

public class TinyTNT extends PrimedTnt {
    public TinyTNT(EntityType<? extends PrimedTnt> entityType, Level level) {
        super(entityType, level);
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
        this.level().explode(this, this.getX(), this.getY(0.0625), this.getZ(), 4.0f, Config.getInstance().weaponsAreDestructive ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
    }
}
