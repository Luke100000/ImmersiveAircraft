package immersive_aircraft.entity.weapon;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.network.s2c.FireResponse;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Map;
import java.util.Random;

public abstract class BulletWeapon extends Weapon {
    private final Random random = new Random();

    private ItemStack ammoStack;
    private int ammo;

    public BulletWeapon(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot) {
        super(entity, stack, mount, slot);
    }

    protected float getBarrelLength() {
        return 1.0f;
    }

    protected immersive_aircraft.compat.Vector4f getBarrelOffset() {
        return new immersive_aircraft.compat.Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
    }

    protected int getBulletCount() {
        return 1;
    }

    public void fire(Vec3d direction) {
        // Calculate the position of the barrel
        immersive_aircraft.compat.Vector4f position = getBarrelOffset();
        VehicleEntity entity = getEntity();
        position.transform(getMount().transform());
        position.transform(entity.getVehicleTransform());

        Vec3d speed = entity.getVelocity();

        // Offset the position by the barrel length
        float barrelLength = getBarrelLength();
        double px = position.getX() + direction.x * barrelLength;
        double py = position.getY() + direction.y * barrelLength;
        double pz = position.getZ() + direction.z * barrelLength;

        // Spawn bullets
        for (int i = 0; i < getBulletCount(); i++) {
            Entity bullet = getBullet(entity, px, py, pz, direction);
            bullet.motionX += speed.x;
            bullet.motionY += speed.y;
            bullet.motionZ += speed.z;
            entity.world.spawnEntity(bullet);
        }

        // Fire-particle
        Vec3d particleVelocity = direction.scale(0.25f).add(speed);
        FireResponse fireMessage = new FireResponse(px, py, pz, particleVelocity.x, particleVelocity.y, particleVelocity.z);
        for (EntityPlayer player : entity.world.playerEntities) {
            if (player instanceof EntityPlayerMP) {
                NetworkHandler.sendToPlayer(fireMessage, (EntityPlayerMP) player);
            }
        }

        // Play sound
        getEntity().playSound(getSound(), 1.0f, random.nextFloat() * 0.2f + 0.9f);
    }

    protected abstract Entity getBullet(Entity shooter, double x, double y, double z, Vec3d direction);

    public SoundEvent getSound() {
        return SoundEvents.ENTITY_ARROW_SHOOT;
    }

    protected boolean spentAmmo(Map<String, Integer> ammunition, int amount) {
        if (ammo < amount && getEntity() instanceof InventoryVehicleEntity) {
            InventoryVehicleEntity vehicle = (InventoryVehicleEntity) getEntity();
            for (int i = 0; i < vehicle.getInventory().getSizeInventory(); i++) {
                ItemStack stack = vehicle.getInventory().getStackInSlot(i);
                if (stack.isEmpty()) {
                    continue;
                }
                String key = Item.REGISTRY.getNameForObject(stack.getItem()).toString();

                if (ammunition.containsKey(key)) {
                    ammoStack = stack.copy();

                    if (!getEntity().isPilotCreative()) {
                        ammo += ammunition.get(key);
                        stack.shrink(1);
                    }
                    break;
                }
            }
        }

        if (getEntity().isPilotCreative()) {
            return true;
        }

        if (ammo <= 0) {
            if (getEntity().getControllingPassenger() instanceof EntityPlayer) {
                ((EntityPlayer) getEntity().getControllingPassenger()).sendStatusMessage(new TextComponentTranslation("immersive_aircraft.out_of_ammo"), true);
            }
            return false;
        }

        ammo -= amount;
        return true;
    }

    public ItemStack getAmmoStack() {
        return ammoStack;
    }

    public int getAmmo() {
        return ammo;
    }
}
