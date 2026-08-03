package immersive_aircraft.entity.weapon;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.bullet.TinyTNT;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.network.c2s.FireMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;

public class BombBay extends BulletWeapon {
    private static final float MAX_COOLDOWN = 1.0f;
    private float cooldown = 0.0f;

    public static final java.util.Map<String, Integer> AMMUNITION = Collections.singletonMap("minecraft:tnt", 100);

    public BombBay(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot) {
        super(entity, stack, mount, slot);
    }

    @Override
    protected float getBarrelLength() {
        return 0.25f;
    }

    @Override
    protected immersive_aircraft.compat.Vector4f getBarrelOffset() {
        return new immersive_aircraft.compat.Vector4f(0.0f, -0.8f, 0.0f, 1.0f);
    }

    public float getVelocity() {
        return 0.0f;
    }

    @Override
    protected Entity getBullet(Entity shooter, double x, double y, double z, Vec3d direction) {
        ItemStack stack = getAmmoStack();

        // 1.20.1 Config.bombBayEntity: {"minecraft:egg": "minecraft:chicken"}, default tiny_tnt
        Entity entity;
        if (stack != null && stack.getItem() == Items.EGG) {
            entity = new EntityChicken(shooter.world);
        } else {
            entity = new TinyTNT(shooter.world);
        }
        entity.setPosition(x, y, z);
        entity.motionX = direction.x * getVelocity();
        entity.motionY = direction.y * getVelocity();
        entity.motionZ = direction.z * getVelocity();
        return entity;
    }

    @Override
    public void tick() {
        cooldown -= 1.0f / 20.0f;
    }

    @Override
    public void fire(Vec3d direction) {
        if (spentAmmo(AMMUNITION, 20)) {
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
        immersive_aircraft.compat.Vec3f direction = new immersive_aircraft.compat.Vec3f(0, 1.0f, 0);
        direction.transform(new immersive_aircraft.compat.Matrix3f(getMount().transform()));
        direction.transform(getEntity().getVehicleNormalTransform());
        return new Vec3d(direction.getX(), direction.getY(), direction.getZ());
    }
}
