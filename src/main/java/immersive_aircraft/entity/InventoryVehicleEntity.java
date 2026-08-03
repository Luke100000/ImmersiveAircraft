package immersive_aircraft.entity;

import immersive_aircraft.GuiHandler;
import immersive_aircraft.Main;
import immersive_aircraft.WeaponRegistry;
import immersive_aircraft.entity.misc.SparseSimpleInventory;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.entity.weapon.Weapon;
import immersive_aircraft.item.UpgradeItem;
import immersive_aircraft.item.WeaponItem;
import immersive_aircraft.item.upgrade.AircraftStat;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class InventoryVehicleEntity extends VehicleEntity {
    protected SparseSimpleInventory inventory;
    protected final Map<Integer, List<Weapon>> weapons = new HashMap<>();
    private static final VehicleInventoryDescription inventoryDescription = new VehicleInventoryDescription()
            .addSlot(VehicleInventoryDescription.SlotType.BOILER, 8 + 9, 8 + 10)
            .build();

    public VehicleInventoryDescription getInventoryDescription() {
        return inventoryDescription;
    }

    public List<ItemStack> getSlots(VehicleInventoryDescription.SlotType slotType) {
        List<VehicleInventoryDescription.Slot> slots = getInventoryDescription().getSlots(slotType);
        List<ItemStack> list = new ArrayList<>(slots.size());
        for (VehicleInventoryDescription.Slot slot : slots) {
            list.add(getInventory().getStack(slot.index));
        }
        return list;
    }

    //todo cache?
    public float getTotalUpgrade(AircraftStat stat) {
        float value = 1.0f;
        List<ItemStack> upgrades = getSlots(VehicleInventoryDescription.SlotType.UPGRADE);
        for (int step = 0; step < 2; step++) {
            for (ItemStack stack : upgrades) {
                if (stack.getItem() instanceof UpgradeItem) {
                    float u = ((UpgradeItem)stack.getItem()).getUpgrade().get(stat);
                    if (u > 0 && step == 1) {
                        value += u;
                    } else if (u < 0 && step == 0) {
                        value *= (u + 1);
                    }
                }
            }
        }
        return Math.max(0.0f, value);
    }

    public InventoryVehicleEntity(World world) {
        super(world);
        this.initInventory();
    }

    protected void initInventory() {
        this.inventory = new SparseSimpleInventory(getInventoryDescription().getInventorySize());
    }

    @Override
    protected void drop() {
        super.drop();

        //drop inventory
        if (this.inventory != null) {
            for (int i = 0; i < this.inventory.size(); ++i) {
                ItemStack itemStack = this.inventory.getStack(i);
                if (itemStack.isEmpty() || EnchantmentHelper.hasVanishingCurse(itemStack)) continue;
                this.entityDropItem(itemStack, 0.0f);
            }
        }
    }

    public void openInventory(EntityPlayerMP player) {
        player.openGui(Main.instance, GuiHandler.GUI_VEHICLE, player.world, getEntityId(), 0, 0);
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (!player.world.isRemote && player.isSneaking()) {
            Entity primaryPassenger = getFirstPassenger();
            if (primaryPassenger != null) {
                // Kick out the first passenger
                primaryPassenger.dismountRidingEntity();
            } else {
                // Open inventory instead
                openInventory((EntityPlayerMP) player);
            }
            return true;
        }
        return super.processInitialInteract(player, hand);
    }


    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);

        NBTTagList nbtList = nbt.getTagList("Inventory", 10);
        this.inventory.readNbt(nbtList);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);

        nbt.setTag("Inventory", this.inventory.writeNbt(new NBTTagList()));
    }

    public SparseSimpleInventory getInventory() {
        return inventory;
    }

    @Override
    public void boost() {
        super.boost();

        getSlots(VehicleInventoryDescription.SlotType.BOOSTER).forEach(s -> s.shrink(1));
    }

    @Override
    protected void applyBoost() {
        super.applyBoost();

        // boost
        Vec3d direction = getDirection();
        float thrust = 0.05f * getBoost() / 100.0f;
        setVelocity(getVelocity().add(direction.scale(thrust)));

        // particles
        if (ticksExisted % 2 == 0) {
            Vec3d p = getPositionVector();
            Vec3d velocity = getVelocity().subtract(direction);
            world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, p.x, p.y, p.z, velocity.x, velocity.y, velocity.z);
        }
    }

    @Override
    public boolean canBoost() {
        return getSlots(VehicleInventoryDescription.SlotType.BOOSTER).stream().anyMatch(v -> !v.isEmpty()) && getBoost() <= 0;
    }

    @Override
    public void onUpdate() {
        inventory.tick(this);

        // Check and recreate weapon slots
        for (VehicleInventoryDescription.Slot slot : getInventoryDescription().getSlots(VehicleInventoryDescription.SlotType.WEAPON)) {
            ItemStack weaponItemStack = getInventory().getStack(slot.index);
            List<Weapon> weapon = weapons.get(slot.index);

            if (weaponItemStack.isEmpty() && weapon != null) {
                weapons.remove(slot.index);
            } else if (!weaponItemStack.isEmpty() && (weapon == null || weapon.get(0).getStack() != weaponItemStack)) {
                WeaponRegistry.WeaponConstructor constructor = WeaponRegistry.get(weaponItemStack);
                if (constructor != null) {
                    List<WeaponMount> weaponMounts = getWeaponMounts(slot.index);
                    ArrayList<Weapon> weapons = new ArrayList<>(weaponMounts.size());
                    for (WeaponMount weaponMount : weaponMounts) {
                        weapons.add(constructor.create(this, weaponItemStack, weaponMount, slot.index));
                    }
                    this.weapons.put(slot.index, weapons);
                }
            }
        }

        // Update gunner offsets
        // The first weapon is assigned to the last passenger, the second to the second last, etc.
        // If more weapons than passengers are available, the remaining weapons are assigned to the driver
        int gunnerOffset = getPassengers().size();
        for (List<Weapon> weapons : getWeapons().values()) {
            gunnerOffset--;
            for (Weapon weapon : weapons) {
                weapon.setGunnerOffset(Math.max(0, gunnerOffset));
            }
        }

        // Update weapons
        for (List<Weapon> weapons : weapons.values()) {
            for (Weapon w : weapons) {
                w.tick();
            }
        }

        super.onUpdate();
    }

    private static final List<WeaponMount> EMPTY_WEAPONS = Collections.emptyList();

    /**
     * Weapon mount definitions per weapon-slot ordinal (baked from the 1.20.1 aircraft jsons).
     */
    protected List<Map<WeaponMount.Type, List<WeaponMount>>> getWeaponMountDefinitions() {
        return Collections.emptyList();
    }

    public List<WeaponMount> getWeaponMounts(int slotIndex) {
        ItemStack stack = getInventory().getStack(slotIndex);
        if (stack.getItem() instanceof WeaponItem) {
            List<VehicleInventoryDescription.Slot> weaponSlots = getInventoryDescription().getSlots(VehicleInventoryDescription.SlotType.WEAPON);
            int ordinal = -1;
            for (int i = 0; i < weaponSlots.size(); i++) {
                if (weaponSlots.get(i).index == slotIndex) {
                    ordinal = i;
                    break;
                }
            }
            List<Map<WeaponMount.Type, List<WeaponMount>>> definitions = getWeaponMountDefinitions();
            if (ordinal >= 0 && ordinal < definitions.size()) {
                List<WeaponMount> mounts = definitions.get(ordinal).get(((WeaponItem) stack.getItem()).getMountType());
                return mounts == null ? EMPTY_WEAPONS : mounts;
            }
        }
        return EMPTY_WEAPONS;
    }

    public Map<Integer, List<Weapon>> getWeapons() {
        return weapons;
    }

    public boolean isScoping() {
        for (List<Weapon> weapons : getWeapons().values()) {
            for (Weapon weapon : weapons) {
                if (weapon instanceof immersive_aircraft.entity.weapon.Telescope && ((immersive_aircraft.entity.weapon.Telescope) weapon).isScoping()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void clientFireWeapons(Entity entity) {
        int gunnerIndex = getPassengers().indexOf(entity);
        for (List<Weapon> weapons : getWeapons().values()) {
            int index = 0;
            for (Weapon weapon : weapons) {
                if (weapon.getGunnerOffset() == gunnerIndex) {
                    weapon.clientFire(index++);
                }
            }
        }
    }

    public void fireWeapon(int slot, int index, net.minecraft.util.math.Vec3d direction) {
        List<Weapon> weaponList = getWeapons().get(slot);
        if (weaponList != null && index < weaponList.size()) {
            weaponList.get(index).fire(direction);
        }
    }

    @Override
    protected float getDurability() {
        return super.getDurability() * getTotalUpgrade(AircraftStat.DURABILITY);
    }
}
