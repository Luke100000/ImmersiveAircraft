package immersive_aircraft.entity.weapons;

import com.mojang.math.*;
import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.bullet.BulletEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.network.c2s.FireMessage;
import immersive_aircraft.network.s2c.FireResponse;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import static immersive_aircraft.Entities.BULLET;

public class RotaryCannon extends Weapon {
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
    public void tick() {
        lastYaw = yaw;
        lastPitch = pitch;
        lastRotating = rotating;

        // todo use the "right" passenger
        Entity pilot = getEntity().getControllingPassenger();
        if (pilot != null) {
            boolean firstPerson = Main.firstPersonGetter.isFirstPerson();
            yaw = (float) ((pilot.getYHeadRot() - getEntity().getYRot()) / 180 * Math.PI);
            pitch = (float) ((pilot.getXRot() - getEntity().getXRot()) / 180 * Math.PI) - (firstPerson ? 0.0f : 0.2f);
        }
    }

    @Override
    public void fire(Vector3f direction) {
        Vector4f position = new Vector4f(0, 1.125f, 0.0f, 1.0f);
        position.transform(getMount().getTransform());
        VehicleEntity entity = getEntity();
        position.transform(entity.getVehicleTransform());

        float barrelLength = 1.0f;
        position.add(direction.x() * barrelLength, direction.y() * barrelLength, direction.z() * barrelLength, 0.0f);

        ItemStack stack = new ItemStack(Items.ARROW.asItem());
        Level level = entity.getLevel();

        Arrow arrow = new Arrow(level, position.x(), position.y(), position.z());
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        arrow.setOwner(entity);
        arrow.shoot(direction.x(), direction.y() + 0.1f, direction.z(), 10.0f, 0.0f);
        //level.addFreshEntity(arrow);

        BulletEntity bullet = BULLET.get().create(level);
        assert bullet != null;
        bullet.setPos(position.x(), position.y(), position.z());
        bullet.setOwner(entity);
        bullet.shoot(direction.x(), direction.y(), direction.z(), 5.0f, 1.0f);
        level.addFreshEntity(bullet);

        stack.shrink(1);

        // Fire-particle
        float vx = (float) (entity.xOld - entity.getX());
        float vy = (float) (entity.yOld - entity.getY());
        float vz = (float) (entity.zOld - entity.getZ());
        direction.mul(0.25f);
        direction.add(vx, vy, vz);
        FireResponse fireMessage = new FireResponse(position, direction);
        for (ServerPlayer player : ((ServerLevel) entity.getLevel()).players()) {
            NetworkHandler.sendToPlayer(fireMessage, player);
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
        quaternion.mul(Quaternion.fromXYZ(getPitch(), 0.0f, 0.0f));
        quaternion.mul(Quaternion.fromXYZ(0.0f, -getYaw(), 0.0f));
        return quaternion;
    }

    public Quaternion getHeadTransform(float tickDelta) {
        Quaternion quaternion = Quaternion.fromXYZ(0.0f, 0.0f, (float) (-getEntity().getRoll(tickDelta) / 180.0 * Math.PI));
        quaternion.mul(Quaternion.fromXYZ(getPitch(tickDelta), 0.0f, 0.0f));
        quaternion.mul(Quaternion.fromXYZ(0.0f, -getYaw(tickDelta), 0.0f));
        quaternion.mul(Quaternion.fromXYZ(0.0f, 0.0f, getRotating(tickDelta)));
        return quaternion;
    }

    @Override
    public void clientFire() {
        float old = rotating;
        rotating += 0.25;

        if (Math.floor(old) != Math.floor(rotating)) {
            NetworkHandler.sendToServer(new FireMessage(getSlot(), getDirection()));
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
