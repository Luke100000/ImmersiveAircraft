package immersive_aircraft.entity.weapon;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.network.c2s.FireMessage;
import immersive_aircraft.resources.bbmodel.BBAnimationVariables;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class HeavyCrossbow extends BulletWeapon {
    private float cooldown = 0.0f;

    private final float velocity;
    private final float inaccuracy;

    // Original mount transform from JSON, saved to restore each tick
    private final Matrix4f baseTransform;

    public HeavyCrossbow(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot) {
        this(entity, stack, mount, slot, Config.getInstance().heavyCrossBowVelocity, Config.getInstance().heavyCrossBowInaccuracy);
    }

    public HeavyCrossbow(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot, float velocity, float inaccuracy) {
        super(entity, stack, mount, slot);

        this.velocity = velocity;
        this.inaccuracy = inaccuracy;

        // Create RotationalManager if this mount has rotation enabled
        if (mount.enableRotation()) {
            rotationalManager = new RotationalManager(this);
            baseTransform = new Matrix4f(mount.transform());
        } else {
            baseTransform = null;
        }
    }

    private float getMaxCooldown() {
        return Config.getInstance().heavyCrossBowCooldown;
    }

    @Override
    protected float getBarrelLength() {
        return 1.25f;
    }

    @Override
    protected Vector4f getBarrelOffset() {
        return new Vector4f(0.0f, 0.3f, 0.0f, 1.0f);
    }

    public float getVelocity() {
        return velocity;
    }

    public float getInaccuracy() {
        return inaccuracy;
    }

    @Override
    protected Entity getBullet(Vector4f position, Vector3f direction) {
        Arrow arrow = new Arrow(getEntity().level(), position.x(), position.y(), position.z(), new ItemStack(net.minecraft.world.item.Items.ARROW), null);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        // Use the pilot as owner so arrows can still collide with the vehicle
        Entity owner = getEntity().getControllingPassenger();
        if (owner == null) owner = getEntity();
        arrow.setOwner(owner);
        // Random velocity spread (75%–125% of base velocity with 0.25 spread)
        float spread = Config.getInstance().heavyCrossBowVelocitySpread;
        float speed = getVelocity() * (1.0f + (getEntity().getRandom().nextFloat() - 0.5f) * 2.0f * spread);
        arrow.shoot(direction.x(), direction.y() + 0.1f, direction.z(), speed, getInaccuracy());
        return arrow;
    }

    @Override
    public void tick() {
        cooldown -= 1.0f / 20.0f;

        if (rotationalManager != null && baseTransform != null) {
            // Restore base transform
            getMount().transform().set(baseTransform);

            rotationalManager.tick();
            rotationalManager.pointTo(getEntity());

            // Clamp angles to mount limits so the crossbow points to the closest
            // valid position when the camera exceeds the reachable range
            WeaponMount mount = getMount();
            float yawDeg = (float) Math.toDegrees(rotationalManager.yaw);
            float pitchDeg = (float) Math.toDegrees(rotationalManager.pitch);

            rotationalManager.yaw = (float) Math.toRadians(Math.max(mount.minYaw(), Math.min(mount.maxYaw(), yawDeg)));
            rotationalManager.pitch = (float) Math.toRadians(Math.max(mount.minPitch(), Math.min(mount.maxPitch(), pitchDeg)));

            mount.transform().rotateX(rotationalManager.pitch);
            mount.transform().rotateY(-rotationalManager.yaw);
        }
    }

    @Override
    public void fire(Vector3f direction) {
        if (spentAmmo(Config.getInstance().arrowAmmunition, 50)) {
            super.fire(direction);
        }
    }

    @Override
    public void clientFire(int index) {
        if (cooldown <= 0.0f) {
            cooldown = getMaxCooldown();
            NetworkHandler.sendToServer(new FireMessage(getSlot(), index, getDirection()));
        }
    }

    protected Vector3f getDirection() {
        // mount.transform() already includes clamped rotation (applied in tick())
        Vector3f direction = new Vector3f(0, 0, 1.0f);
        direction.mul(new Matrix3f(getMount().transform()));
        direction.mul(getEntity().getVehicleNormalTransform());
        return direction;
    }

    @Override
    public <T extends VehicleEntity> void setAnimationVariables(T entity, float time) {
        super.setAnimationVariables(entity, time);

        if (rotationalManager != null) {
            float tickDelta = time % 1.0f;
            BBAnimationVariables.set("pitch", (float) (rotationalManager.getPitch(tickDelta) / Math.PI * 180.0f));
            BBAnimationVariables.set("yaw", (float) (rotationalManager.getYaw(tickDelta) / Math.PI * 180.0f));
        }
    }

    public float getCooldown() {
        return Math.max(0.0f, cooldown / getMaxCooldown());
    }
}