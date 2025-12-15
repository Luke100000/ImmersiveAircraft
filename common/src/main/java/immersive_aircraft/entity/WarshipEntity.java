package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.Main;
import immersive_aircraft.Sounds;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.entity.weapon.HeavyCrossbow;
import immersive_aircraft.resources.bbmodel.AnimationVariableName;
import immersive_aircraft.resources.bbmodel.BBAnimationVariables;
import immersive_aircraft.util.InterpolatedFloat;
import immersive_aircraft.util.Utils;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.joml.Math;

import java.util.List;

import static immersive_aircraft.resources.bbmodel.AnimationVariableName.*;
import static immersive_aircraft.resources.bbmodel.AnimationVariableName.BALLOON_ROLL;
import static immersive_aircraft.resources.bbmodel.AnimationVariableName.TURRET_PITCH;
import static immersive_aircraft.resources.bbmodel.AnimationVariableName.TURRET_YAW;

public class WarshipEntity extends AirshipEntity {
    private final HeavyCrossbow turret;

    public WarshipEntity(EntityType<? extends AircraftEntity> entityType, Level world) {
        super(entityType, world);

        turret = new HeavyCrossbow(this, new ItemStack(Items.HEAVY_CROSSBOW.get()), new WeaponMount(new Matrix4f(), false), -1, 5.0f, 0.0f);
    }

    public final InterpolatedFloat turretYaw = new InterpolatedFloat(5);
    public final InterpolatedFloat turretPitch = new InterpolatedFloat(5);

    @Override
    public Item asItem() {
        return Items.WARSHIP.get();
    }

    @Override
    public void setAnimationVariables(BBAnimationVariables animationVariables, float tickDelta) {
        super.setAnimationVariables(animationVariables, tickDelta);

        animationVariables.set(TURRET_YAW, -turretYaw.getSmooth(tickDelta));
        animationVariables.set(TURRET_PITCH, -turretPitch.getSmooth(tickDelta));

        if (weapons.isEmpty()) {
            animationVariables.set(BALLOON_ROLL, (float) Utils.cosNoise((tickCount + tickDelta) * 0.01f) * 0.2f + getRoll(tickDelta) * 0.5f);
            animationVariables.set(BALLOON_PITCH, (float) Utils.cosNoise(77.0f + (tickCount + tickDelta) * 0.02f) * 0.2f);
        } else {
            // Weapon mounts would detach the vehicle if the vehicle is not moving
            animationVariables.set(BALLOON_ROLL, 0.0f);
            animationVariables.set(BALLOON_PITCH, 0.0f);
        }

        animationVariables.set(CHEST, (float) Math.max(0.0, this.getSpeedVector().y));
        animationVariables.set(TURRET_COOLDOWN, turret.getCooldown());
    }

    @Override
    protected float getEngineReactionSpeed() {
        return 100.0f;
    }

    @Override
    protected SoundEvent getEngineSound() {
        return Sounds.WARSHIP.get();
    }

    @Override
    protected SoundEvent getEngineStartSound() {
        return Sounds.ENGINE_START_WARSHIP.get();
    }

    public Vector3f boneOffset(Vector3f position, Vector3f rotation, Vector3f origin) {
        position.sub(origin);
        position = Utils.fromXYZ(rotation).transform(position);
        position.add(origin);
        return position;
    }

    public Entity getTurretGunner() {
        List<Entity> passengers = getPassengers();
        if (passengers.size() >= 2) {
            return passengers.get(1);
        } else {
            return null;
        }
    }

    public boolean isTurretGunner(Entity entity) {
        return getTurretGunner() == entity;
    }

    public void positionRider(@NotNull Entity passenger, @NotNull MoveFunction positionUpdater) {
        Matrix4f transform = getVehicleTransform();

        if (isTurretGunner(passenger)) {
            Vector3f position = getGunnerPosition();

            float x = position.x();
            float y = position.y();
            float z = position.z();

            Vec3 attachmentPoint = passenger.getVehicleAttachmentPoint(this);
            x -= (float) attachmentPoint.x;
            y -= (float) attachmentPoint.y;
            z -= (float) attachmentPoint.z;

            Vector4f worldPosition = transformPosition(transform, x, y, z);
            positionUpdater.accept(passenger, worldPosition.x, worldPosition.y, worldPosition.z);
            copyEntityData(passenger);
        } else {
            super.positionRider(passenger, positionUpdater);
        }
    }

    private Vector3f getGunnerPosition() {
        float pitch = turretPitch.getSmooth(1.0f) / 180.0f * (float) Math.PI;
        float yaw = turretYaw.getSmooth(1.0f) / 180.0f * (float) Math.PI;

        Vector3f position = new Vector3f(0.0f, 0.5f, 2.5f);
        boneOffset(position, new Vector3f(0.0f, -yaw, 0.0f), new Vector3f(0.0f, 0.5f, 2.0f));
        boneOffset(position, new Vector3f(pitch, 0.0f, 0.0f), new Vector3f(0.0f, 0.5f, 0.9f));

        return position;
    }

    public void copyEntityData(Entity entity) {
        if (isTurretGunner(entity)) {
            entity.setYBodyRot(getYRot() + turretYaw.getSmooth(1.0f));

            float py = Mth.wrapDegrees(entity.getYRot() - getYRot());
            float cpy = Mth.clamp(py, -45f, 45f);
            entity.yRotO += cpy - py;
            entity.setYRot(entity.getYRot() + cpy - py);
            entity.setYHeadRot(entity.getYRot());

            float pp = Mth.wrapDegrees(entity.getXRot() - getXRot());
            float cpp = Mth.clamp(pp, -30f, 30f);
            entity.xRotO += cpp - pp;
            entity.setXRot(entity.getXRot() + cpp - pp);
        } else {
            super.copyEntityData(entity);
        }
    }

    @Override
    public void tick() {
        super.tick();

        turret.tick();

        Entity gunner = getTurretGunner();
        if (gunner != null) {
            turretYaw.update(Math.clamp(-75f, 75f, Mth.wrapDegrees(gunner.getYHeadRot() - getYRot())));
            turretPitch.update(Math.clamp(-30f, 30f, gunner.getXRot()));
        } else {
            turretYaw.update(0.0f);
            turretPitch.update(0.0f);
        }

        // Rotate turret
        Vector3f gunnerPosition = getGunnerPosition();
        Quaternionf turretRot = Utils.fromXYZ(
                Math.toRadians(turretPitch.get(1.0f)),
                Math.toRadians(-turretYaw.get(1.0f)),
                0.0f
        );
        turret.getMount().transform().identity().translate(gunnerPosition).rotate(turretRot);
    }

    @Override
    public void clientFireWeapons(Entity entity) {
        if (isTurretGunner(entity)) {
            turret.clientFire(-1);
        } else {
            super.clientFireWeapons(entity);
        }
    }

    @Override
    public void fireWeapon(int slot, int index, Vector3f direction) {
        if (slot == -1) {
            turret.fire(direction);
        } else {
            super.fireWeapon(slot, index, direction);
        }
    }

    @Override
    public double getZoom() {
        return 5.0f + enginePower.getSmooth(Main.frameTime) * 5.0f;
    }
}
