package immersive_aircraft.entity.weapon;

import immersive_aircraft.Sounds;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.bullet.BulletEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.network.c2s.FireMessage;
import immersive_aircraft.resources.bbmodel.AnimationVariableName;
import immersive_aircraft.resources.bbmodel.BBAnimationVariables;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static immersive_aircraft.Entities.BULLET;

public class RotaryCannon extends BulletWeapon {
    private final RotationalManager rotationalManager = new RotationalManager(this);

    public RotaryCannon(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot) {
        super(entity, stack, mount, slot);
    }

    @Override
    protected Vector4f getBarrelOffset() {
        return new Vector4f(0.0f, 0.825f, -0.375f, 1.0f);
    }

    public float getVelocity() {
        return 4.0f;
    }

    public float getInaccuracy() {
        return 1.0f;
    }

    @Override
    protected Entity getBullet(Vector4f position, Vector3f direction) {
        BulletEntity bullet = BULLET.get().create(getEntity().level(), EntitySpawnReason.TRIGGERED);
        assert bullet != null;
        bullet.setDamage(Config.getInstance().rotaryCannonDamage);
        bullet.setPos(position.x(), position.y(), position.z());
        bullet.setOwner(getEntity().getControllingPassenger());
        bullet.shoot(direction.x(), direction.y(), direction.z(), getVelocity(), getInaccuracy());
        return bullet;
    }

    @Override
    public void tick() {
        rotationalManager.tick();
        rotationalManager.pointTo(getEntity());
    }

    @Override
    public void fire(Vector3f direction) {
        if (spentAmmo(Config.getInstance().gunpowderAmmunition, 10)) {
            super.fire(direction);
        }
    }

    @Override
    public SoundEvent getSound() {
        return Sounds.CANNON.get();
    }

    private Vector3f getDirection() {
        return rotationalManager.screenToGlobal(getEntity());
    }

    @Override
    public void clientFire(int index) {
        float old = rotationalManager.roll;
        rotationalManager.roll += 0.25f;

        if (Math.floor(old) != Math.floor(rotationalManager.roll)) {
            NetworkHandler.sendToServer(new FireMessage(getSlot(), index, getDirection()));
        }
    }

    @Override
    public void setAnimationVariables(BBAnimationVariables vars, float time) {
        super.setAnimationVariables(vars, time);
        float tickDelta = time % 1.0f;
        vars.set(AnimationVariableName.PITCH, (float) (rotationalManager.getPitch(tickDelta) / Math.PI * 180.0f));
        vars.set(AnimationVariableName.YAW, (float) (rotationalManager.getYaw(tickDelta) / Math.PI * 180.0f));
        vars.set(AnimationVariableName.ROLL, (float) (rotationalManager.getRoll(tickDelta) / Math.PI * 180.0f));
    }
}
