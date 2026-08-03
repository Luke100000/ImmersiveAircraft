package immersive_aircraft.entity.weapon;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.network.c2s.FireMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HeavyCrossbow extends BulletWeapon {
    private static final float MAX_COOLDOWN = 1.0f;
    private float cooldown = 0.0f;

    /**
     * 1.20.1 Config.heavyCrossBowVelocity (3.0); the velocity also determines the arrow's damage
     */
    public static final float DEFAULT_VELOCITY = 3.0f;

    public static final Map<String, Integer> AMMUNITION;

    static {
        Map<String, Integer> map = new HashMap<>();
        map.put("minecraft:arrow", 100);
        map.put("minecraft:tipped_arrow", 100);
        map.put("minecraft:spectral_arrow", 100);
        AMMUNITION = Collections.unmodifiableMap(map);
    }

    private final float velocity;
    private final float inaccuracy;

    public HeavyCrossbow(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot) {
        this(entity, stack, mount, slot, DEFAULT_VELOCITY, 0.0f);
    }

    public HeavyCrossbow(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot, float velocity, float inaccuracy) {
        super(entity, stack, mount, slot);

        this.velocity = velocity;
        this.inaccuracy = inaccuracy;
    }

    @Override
    protected float getBarrelLength() {
        return 1.25f;
    }

    @Override
    protected immersive_aircraft.compat.Vector4f getBarrelOffset() {
        return new immersive_aircraft.compat.Vector4f(0.0f, 0.3f, 0.0f, 1.0f);
    }

    public float getVelocity() {
        return velocity;
    }

    public float getInaccuracy() {
        return inaccuracy;
    }

    @Override
    protected Entity getBullet(Entity shooter, double x, double y, double z, Vec3d direction) {
        ItemStack ammoStack = getAmmoStack();
        EntityArrow arrow;
        if (ammoStack != null && ammoStack.getItem() == Items.SPECTRAL_ARROW) {
            arrow = new EntitySpectralArrow(shooter.world);
        } else {
            arrow = new EntityTippedArrow(shooter.world);
            if (ammoStack != null && ammoStack.getItem() == Items.TIPPED_ARROW) {
                ((EntityTippedArrow) arrow).setPotionEffect(ammoStack);
            }
        }
        arrow.setPosition(x, y, z);
        arrow.pickupStatus = EntityArrow.PickupStatus.DISALLOWED;
        arrow.shootingEntity = getEntity().getControllingPassenger();
        arrow.shoot(direction.x, direction.y + 0.1f, direction.z, getVelocity(), getInaccuracy());
        return arrow;
    }

    @Override
    public void tick() {
        cooldown -= 1.0f / 20.0f;
    }

    @Override
    public void fire(Vec3d direction) {
        if (spentAmmo(AMMUNITION, 50)) {
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

    private Vec3d getDirection() {
        immersive_aircraft.compat.Vec3f direction = new immersive_aircraft.compat.Vec3f(0, 0, 1.0f);
        direction.transform(new immersive_aircraft.compat.Matrix3f(getMount().transform()));
        direction.transform(getEntity().getVehicleNormalTransform());
        return new Vec3d(direction.getX(), direction.getY(), direction.getZ());
    }

    public float getCooldown() {
        return Math.max(0.0f, cooldown / MAX_COOLDOWN);
    }
}
