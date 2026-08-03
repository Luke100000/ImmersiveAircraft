package immersive_aircraft.entity.weapon;

import immersive_aircraft.Sounds;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.bullet.BulletEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.network.c2s.FireMessage;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;

public class RotaryCannon extends BulletWeapon {
    /**
     * 1.20.1 Config.rotaryCannonDamage (5.0)
     */
    public static final float DAMAGE = 5.0f;

    public static final java.util.Map<String, Integer> AMMUNITION = Collections.singletonMap("minecraft:gunpowder", 100);

    private final RotationalManager rotationalManager = new RotationalManager(this);

    public RotaryCannon(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot) {
        super(entity, stack, mount, slot);
    }

    @Override
    protected immersive_aircraft.compat.Vector4f getBarrelOffset() {
        return new immersive_aircraft.compat.Vector4f(0.0f, 0.825f, -0.375f, 1.0f);
    }

    public float getVelocity() {
        return 4.0f;
    }

    public float getInaccuracy() {
        return 1.0f;
    }

    @Override
    protected Entity getBullet(Entity shooter, double x, double y, double z, Vec3d direction) {
        BulletEntity bullet = new BulletEntity(shooter.world);
        bullet.setDamage(DAMAGE);
        bullet.setPosition(x, y, z);
        bullet.setOwner(shooter);
        bullet.shoot(direction.x, direction.y, direction.z, getVelocity(), getInaccuracy());
        return bullet;
    }

    @Override
    public void tick() {
        rotationalManager.tick();
        rotationalManager.pointTo(getEntity());
    }

    @Override
    public void fire(Vec3d direction) {
        if (spentAmmo(AMMUNITION, 10)) {
            super.fire(direction);
        }
    }

    @Override
    public SoundEvent getSound() {
        return Sounds.CANNON.get();
    }

    private Vec3d getDirection() {
        immersive_aircraft.compat.Vec3f direction = rotationalManager.screenToGlobal(getEntity());
        return new Vec3d(direction.getX(), direction.getY(), direction.getZ());
    }

    @Override
    public void clientFire(int index) {
        float old = rotationalManager.roll;
        rotationalManager.roll += 0.25f;

        if (Math.floor(old) != Math.floor(rotationalManager.roll)) {
            NetworkHandler.sendToServer(new FireMessage(getSlot(), index, getDirection()));
        }
    }

    public RotationalManager getRotationalManager() {
        return rotationalManager;
    }
}
