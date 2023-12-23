package immersive_aircraft.entity.weapons;

import com.mojang.math.*;
import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.bullet.BulletEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.network.c2s.FireMessage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import static immersive_aircraft.Entities.BULLET;

public class RotaryCannon extends BulletWeapon {
    float yaw = 0.0f;
    float pitch = 0.0f;
    float rotating = 0.0f;

    float lastYaw = 0.0f;
    float lastPitch = 0.0f;
    float lastRotating = 0.0f;

    public RotaryCannon(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot) {
        super(entity, stack, mount, slot);
    }

    @Override
    protected float getBarrelLength() {
        return 1.0f;
    }

    @Override
    protected Vector4f getBarrelOffset() {
        return new Vector4f(0.0f, 1.125f, 0.0f, 1.0f);
    }

    public float getVelocity() {
        return 3.0f;
    }

    public float getInaccuracy() {
        return 0.0f;
    }

    @Override
    protected Entity getBullet(Entity shooter, Vector4f position, Vector3f direction) {
        BulletEntity bullet = BULLET.get().create(shooter.getLevel());
        assert bullet != null;
        bullet.setPos(position.x(), position.y(), position.z());
        bullet.setOwner(shooter);
        bullet.shoot(direction.x(), direction.y(), direction.z(), getVelocity(), getInaccuracy());
        return bullet;
    }

    @Override
    public void tick() {
        lastYaw = yaw;
        lastPitch = pitch;
        lastRotating = rotating;

        // todo use the "right" passenger
        Entity pilot = getEntity().getControllingPassenger();
        if (pilot != null) {
            boolean firstPerson = Main.firstPersonGetter.isFirstPerson();
            yaw = (float) ((pilot.getYHeadRot() - getEntity().getYRot()) / 180 * Math.PI);
            pitch = (float) ((pilot.getXRot() / 180 * Math.PI) - (firstPerson ? 0.0f : 0.2f));
        }
    }

    @Override
    public void fire(Vector3f direction) {
        if (spentAmmo(Config.getInstance().powderAmmunition, 20)) {
            super.fire(direction);
        }
    }

    private Vector3f getDirection() {
        Matrix4f transform = new Matrix4f(getMount().getTransform());
        transform.multiply(getHeadTransform());
        Vector3f direction = new Vector3f(0, 0, 1.0f);
        direction.transform(new Matrix3f(transform));
        direction.transform(getEntity().getVehicleNormalTransform());
        return direction;
    }

    public Quaternion getHeadTransform() {
        Quaternion quaternion = Quaternion.fromXYZ(0.0f, 0.0f, (float) (-getEntity().getRoll() / 180.0 * Math.PI));
        quaternion.mul(Quaternion.fromXYZ(0.0f, -getYaw(), 0.0f));
        quaternion.mul(Quaternion.fromXYZ(getPitch(), 0.0f, 0.0f));
        return quaternion;
    }

    public Quaternion getHeadTransform(float tickDelta) {
        Quaternion quaternion = Quaternion.fromXYZ(0.0f, 0.0f, (float) (-getEntity().getRoll(tickDelta) / 180.0 * Math.PI));
        quaternion.mul(Quaternion.fromXYZ(0.0f, -getYaw(tickDelta), 0.0f));
        quaternion.mul(Quaternion.fromXYZ(getPitch(tickDelta), 0.0f, 0.0f));
        quaternion.mul(Quaternion.fromXYZ(0.0f, 0.0f, getRotating(tickDelta)));
        return quaternion;
    }

    @Override
    public void clientFire(int index) {
        float old = rotating;
        rotating += 0.25f;

        if (Math.floor(old) != Math.floor(rotating)) {
            NetworkHandler.sendToServer(new FireMessage(getSlot(), index, getDirection()));
        }
    }

    public float getYaw() {
        return yaw;
    }

    public float getYaw(float tickDelta) {
        return yaw * tickDelta + lastYaw * (1.0f - tickDelta);
    }

    public float getPitch() {
        return pitch;
    }

    public float getPitch(float tickDelta) {
        return pitch * tickDelta + lastPitch * (1.0f - tickDelta);
    }

    public float getRotating(float tickDelta) {
        return (float) ((rotating * tickDelta + lastRotating * (1.0f - tickDelta)) * Math.PI / 2.0);
    }
}
