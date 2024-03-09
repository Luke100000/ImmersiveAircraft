package immersive_aircraft.entity.weapons;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.network.c2s.FireMessage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class HeavyCrossbow extends BulletWeapon {
    private static final float MAX_COOLDOWN = 1.0f;
    private float cooldown = 0.0f;

    public HeavyCrossbow(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot) {
        super(entity, stack, mount, slot);
    }

    @Override
    protected float getBarrelLength() {
        return 1.25f;
    }

    @Override
    protected Vector4f getBarrelOffset() {
        return new Vector4f(0.0f, 0.7f, 0.0f, 1.0f);
    }

    public float getVelocity() {
        return 3.0f;
    }

    public float getInaccuracy() {
        return 0.0f;
    }

    @Override
    protected Entity getBullet(Entity shooter, Vector4f position, Vector3f direction) {
        Arrow arrow = new Arrow(shooter.getLevel(), position.x(), position.y(), position.z());
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        arrow.setOwner(getEntity().getControllingPassenger());
        arrow.shoot(direction.x(), direction.y() + 0.1f, direction.z(), getVelocity(), getInaccuracy());
        ItemStack ammoStack = getAmmoStack();
        if (ammoStack != null) {
            arrow.setEffectsFromItem(ammoStack);
        }
        return arrow;
    }

    @Override
    public void tick() {
        cooldown -= 1.0f / 20.0f;
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
            cooldown = MAX_COOLDOWN;
            NetworkHandler.sendToServer(new FireMessage(getSlot(), index, getDirection()));
        }
    }

    private Vector3f getDirection() {
        Vector3f direction = new Vector3f(0, 0, 1.0f);
        direction.mul(new Matrix3f(getMount().transform()));
        direction.mul(getEntity().getVehicleNormalTransform());
        return direction;
    }
}
