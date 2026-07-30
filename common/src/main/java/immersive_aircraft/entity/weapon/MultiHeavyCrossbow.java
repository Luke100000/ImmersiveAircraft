package immersive_aircraft.entity.weapon;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.network.c2s.FireMessage;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class MultiHeavyCrossbow extends HeavyCrossbow {
    private float cooldown = 0.0f;

    public MultiHeavyCrossbow(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot) {
        super(entity, stack, mount, slot,
                Config.getInstance().multiHeavyCrossBowVelocity,
                Config.getInstance().multiHeavyCrossBowInaccuracy);
    }

    @Override
    protected int getBulletCount() {
        return Config.getInstance().multiHeavyCrossBowBulletCount;
    }

    private float getMaxCooldown() {
        return Config.getInstance().multiHeavyCrossBowCooldown;
    }

    @Override
    protected Entity getBullet(Vector4f position, Vector3f direction) {
        Arrow arrow = new Arrow(getEntity().level(), position.x(), position.y(), position.z(), ItemStack.EMPTY, null);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        // Set owner to the vehicle itself so arrows don't collide with it
        arrow.setOwner(getEntity());

        // Speed is 1.5x less than heavy crossbow
        float speed = getVelocity();

        // Add random velocity spread (±25%)
        float spread = Config.getInstance().heavyCrossBowVelocitySpread;
        speed *= (1.0f + (getEntity().getRandom().nextFloat() - 0.5f) * 2.0f * spread);

        // Add random direction deviation for the "fan" effect (huge spread)
        Vector3f spreadDirection = new Vector3f(direction);
        spreadDirection.rotateY((getEntity().getRandom().nextFloat() - 0.5f) * 0.5f); // yaw spread
        spreadDirection.rotateX((getEntity().getRandom().nextFloat() - 0.5f) * 0.3f); // pitch spread

        arrow.shoot(spreadDirection.x(), spreadDirection.y() + 0.1f, spreadDirection.z(), speed, getInaccuracy());
        return arrow;
    }

    @Override
    public void tick() {
        super.tick();
        cooldown -= 1.0f / 20.0f;
    }

    @Override
    public void fire(Vector3f direction) {
        // Consume one actual arrow per spawned projectile.
        if (spentAmmoItems(Config.getInstance().arrowAmmunition, getBulletCount())) {
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

    @Override
    public float getCooldown() {
        return Math.max(0.0f, cooldown / getMaxCooldown());
    }
}
