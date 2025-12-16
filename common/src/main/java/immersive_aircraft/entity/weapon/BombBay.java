package immersive_aircraft.entity.weapon;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.network.c2s.FireMessage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static net.minecraft.world.entity.item.PrimedTnt.TAG_FUSE;

public class BombBay extends BulletWeapon {
    private static final float MAX_COOLDOWN = 1.0f;
    private float cooldown = 0.0f;

    public BombBay(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot) {
        super(entity, stack, mount, slot);
    }

    @Override
    protected float getBarrelLength() {
        return 0.25f;
    }

    @Override
    protected Vector4f getBarrelOffset() {
        return new Vector4f(0.0f, -0.8f, 0.0f, 1.0f);
    }

    public float getVelocity() {
        return 0.0f;
    }

    @Override
    protected Entity getBullet(Vector4f position, Vector3f direction) {
        Vector3f vel = direction.mul(getVelocity(), new Vector3f());

        ItemStack stack = getAmmoStack();
        String string = stack != null ? BuiltInRegistries.ITEM.getKey(stack.getItem()).toString() : "minecraft:tnt";
        String identifier = Config.getInstance().bombBayEntity.getOrDefault(string, "immersive_aircraft:tiny_tnt");
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("id", identifier);
        compoundTag.putInt(TAG_FUSE, 80);
        return EntityType.loadEntityRecursive(compoundTag, getEntity().level(), EntitySpawnReason.TRIGGERED, (e) -> {
            e.snapTo(position.x(), position.y(), position.z(), e.getYRot(), e.getXRot());
            e.setDeltaMovement(vel.x(), vel.y(), vel.z());
            return e;
        });
    }

    @Override
    public void tick() {
        cooldown -= 1.0f / 20.0f;
    }

    @Override
    public void fire(Vector3f direction) {
        if (spentAmmo(Config.getInstance().bombBayAmmunition, 20)) {
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
        Vector3f direction = new Vector3f(0, 1.0f, 0);
        direction.mul(new Matrix3f(getMount().transform()));
        direction.mul(getEntity().getVehicleNormalTransform());
        return direction;
    }
}
