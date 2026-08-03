package immersive_aircraft.entity.bullet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * 1.20.1 BulletEntity port: gravity-less straight-flying projectile that damages the
 * first entity hit and dies on any impact. 1.12.2 has no AbstractHurtingProjectile
 * equivalent with this behavior, so the tick is hand-rolled (arrow-style hit scan).
 */
public class BulletEntity extends Entity {
    private float damage = 1.0f;
    private Entity owner;

    public BulletEntity(World world) {
        super(world);
        setSize(0.25f, 0.25f);
    }

    @Override
    protected void entityInit() {
    }

    public float getScale() {
        return 0.25f;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setOwner(Entity owner) {
        this.owner = owner;
    }

    public Entity getOwner() {
        return owner;
    }

    /**
     * EntityArrow-style shoot: aim along (x, y, z) with the given velocity and inaccuracy.
     */
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        float f = MathHelper.sqrt(x * x + y * y + z * z);
        x = x / f;
        y = y / f;
        z = z / f;
        x = x + this.rand.nextGaussian() * 0.0075 * inaccuracy;
        y = y + this.rand.nextGaussian() * 0.0075 * inaccuracy;
        z = z + this.rand.nextGaussian() * 0.0075 * inaccuracy;
        this.motionX = x * velocity;
        this.motionY = y * velocity;
        this.motionZ = z * velocity;
        float f1 = MathHelper.sqrt(x * x + z * z);
        this.rotationYaw = (float) (MathHelper.atan2(x, z) * (180.0 / Math.PI));
        this.rotationPitch = (float) (MathHelper.atan2(y, f1) * (180.0 / Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
    }

    protected boolean canHitEntity(Entity target) {
        if ((target instanceof net.minecraft.entity.player.EntityPlayer && ((net.minecraft.entity.player.EntityPlayer) target).isSpectator()) || !target.isEntityAlive() || !target.canBeCollidedWith()) {
            return false;
        }
        return owner == null || !owner.isRidingSameEntity(target);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (motionX * motionX + motionY * motionY + motionZ * motionZ < 0.1) {
            setDead();
            return;
        }

        Vec3d from = new Vec3d(posX, posY, posZ);
        Vec3d to = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);

        // Block collision
        RayTraceResult blockHit = world.rayTraceBlocks(from, to, false, true, false);
        if (blockHit != null) {
            to = new Vec3d(blockHit.hitVec.x, blockHit.hitVec.y, blockHit.hitVec.z);
        }

        // Entity collision
        if (!world.isRemote) {
            Entity hitEntity = null;
            double hitDistance = Double.MAX_VALUE;
            List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().expand(motionX, motionY, motionZ).grow(1.0));
            for (Entity entity : list) {
                if (!canHitEntity(entity)) {
                    continue;
                }
                AxisAlignedBB box = entity.getEntityBoundingBox().grow(0.3);
                RayTraceResult result = box.calculateIntercept(from, to);
                if (result != null) {
                    double distance = from.distanceTo(result.hitVec);
                    if (distance < hitDistance) {
                        hitEntity = entity;
                        hitDistance = distance;
                    }
                }
            }

            if (hitEntity != null) {
                hitEntity.attackEntityFrom(DamageSource.causeThrownDamage(this, owner), damage);
                setDead();
                return;
            }
        }

        if (blockHit != null) {
            if (!world.isRemote) {
                setDead();
            }
            return;
        }

        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        setPosition(posX, posY, posZ);
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        double d = getEntityBoundingBox().getAverageEdgeLength() * 10.0;
        if (Double.isNaN(d)) {
            d = 10.0;
        }
        d = d * 64.0 * getScale();
        return distance < d * d;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setFloat("damage", damage);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        damage = compound.getFloat("damage");
    }
}
