package immersive_aircraft.entity.bullet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

/**
 * 1.20.1 TinyTNT port: small falling TNT with a short fuse (burns 5x faster on ground).
 * Explosion strength 4.0; block damage gated on the (1.20.1-config, here constant)
 * weaponsAreDestructive flag.
 */
public class TinyTNT extends Entity {
    /**
     * 1.20.1 Config.weaponsAreDestructive (false)
     */
    public static final boolean WEAPONS_ARE_DESTRUCTIVE = false;

    private int fuse = 80;

    public TinyTNT(World world) {
        super(world);
        setSize(0.375f, 0.375f);
    }

    @Override
    protected void entityInit() {
    }

    public int getFuse() {
        return fuse;
    }

    public void setFuse(int fuse) {
        this.fuse = fuse;
    }

    @Override
    public void onUpdate() {
        this.motionY -= 0.04;
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.98;
        this.motionY *= 0.98;
        this.motionZ *= 0.98;
        if (this.onGround) {
            this.motionX *= 0.7;
            this.motionZ *= 0.7;
            this.motionY *= -0.5;
        }

        fuse -= onGround ? 5 : 1;
        if (fuse <= 0) {
            setDead();
            if (!this.world.isRemote) {
                boom();
            }
        } else {
            handleWaterMovement();
            if (this.world.isRemote) {
                this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY + 0.5, posZ, 0.0, 0.0, 0.0);
            }
        }
    }

    private void boom() {
        this.world.newExplosion(this, posX, posY + (double) (height * 0.0625f), posZ, 4.0f, false, WEAPONS_ARE_DESTRUCTIVE);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setShort("Fuse", (short) fuse);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        fuse = compound.getShort("Fuse");
    }
}
