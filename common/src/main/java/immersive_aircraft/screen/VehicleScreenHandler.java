package immersive_aircraft.screen;

import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.item.WeaponItem;
import immersive_aircraft.screen.slot.FuelSlot;
import immersive_aircraft.screen.slot.TypedSlot;
import immersive_aircraft.screen.slot.UpgradeSlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;

public class VehicleScreenHandler extends AbstractContainerMenu {
    private final Container inventory;

    private final InventoryVehicleEntity vehicle;

    public VehicleScreenHandler(int syncId, Inventory playerInventory, InventoryVehicleEntity vehicle) {
        super(null, syncId);

        this.vehicle = vehicle;
        this.inventory = vehicle.getInventory();

        inventory.startOpen(playerInventory.player);

        int titleHeight = 10;

        // Vehicle inventory
        for (VehicleInventoryDescription.Slot slot : this.vehicle.getInventoryDescription().getSlots()) {
            if (slot.type() == VehicleInventoryDescription.SlotType.BOILER) {
                this.addSlot(new FuelSlot(inventory, slot.index(), slot.x(), slot.y() + titleHeight));
            } else if (slot.type() == VehicleInventoryDescription.SlotType.WEAPON) {
                this.addSlot(new TypedSlot(WeaponItem.class, 1, inventory, slot.index(), slot.x(), slot.y() + titleHeight));
            } else if (slot.type() == VehicleInventoryDescription.SlotType.UPGRADE) {
                this.addSlot(new UpgradeSlot(vehicle, 1, inventory, slot.index(), slot.x(), slot.y() + titleHeight));
            } else if (slot.type() == VehicleInventoryDescription.SlotType.BOOSTER) {
                this.addSlot(new TypedSlot(FireworkRocketItem.class, 64, inventory, slot.index(), slot.x(), slot.y() + titleHeight));
            } else if (slot.type() == VehicleInventoryDescription.SlotType.BANNER) {
                this.addSlot(new TypedSlot(BannerItem.class, 1, inventory, slot.index(), slot.x(), slot.y() + titleHeight));
            } else if (slot.type() == VehicleInventoryDescription.SlotType.DYE) {
                this.addSlot(new TypedSlot(DyeItem.class, 1, inventory, slot.index(), slot.x(), slot.y() + titleHeight));
            } else {
                this.addSlot(new Slot(inventory, slot.index(), slot.x(), slot.y() + titleHeight));
            }
        }

        int h = this.vehicle.getInventoryDescription().getHeight() + titleHeight * 2;

        // The player inventory
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, h + y * 18));
            }
        }

        // The player Hotbar
        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, h + 58));
        }
    }

    public boolean stillValid(Player player) {
        return vehicle.getInventory() == this.inventory && this.inventory.stillValid(player) && vehicle.isAlive() && vehicle.distanceTo(player) < 8.0F;
    }

    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();
            if (index < this.inventory.getContainerSize()) {
                if (!this.moveItemStackTo(originalStack, this.inventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(originalStack, 0, this.inventory.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return newStack;
    }

    // Overwritten since max stack size isn't considered in vanilla
    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean unused) {
        boolean inserted = false;

        // try to stack
        if (stack.isStackable()) {
            int i = startIndex;
            while (!stack.isEmpty() && (i < endIndex)) {
                Slot slot = this.slots.get(i);
                ItemStack target = slot.getItem();
                if (!target.isEmpty() && ItemStack.isSameItemSameTags(stack, target)) {
                    int diff = target.getCount() + stack.getCount();
                    int maxCount = slot.getMaxStackSize(stack);
                    if (diff <= maxCount) {
                        stack.setCount(0);
                        target.setCount(diff);
                        slot.setChanged();
                        inserted = true;
                    } else if (target.getCount() < maxCount) {
                        stack.shrink(maxCount - target.getCount());
                        target.setCount(maxCount);
                        slot.setChanged();
                        inserted = true;
                    }
                }
                i++;
            }
        }

        // use a new slot
        if (!stack.isEmpty()) {
            for (int i = startIndex; i < endIndex; i++) {
                Slot slot = this.slots.get(i);
                ItemStack target = slot.getItem();
                int maxCount = slot.getMaxStackSize(target);
                if (target.isEmpty() && slot.mayPlace(stack)) {
                    if (stack.getCount() > maxCount) {
                        slot.set(stack.split(maxCount));
                    } else {
                        slot.set(stack.split(stack.getCount()));
                    }
                    slot.setChanged();
                    inserted = true;
                    break;
                }
            }
        }
        return inserted;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.inventory.stopOpen(player);
    }

    public InventoryVehicleEntity getVehicle() {
        return vehicle;
    }
}
