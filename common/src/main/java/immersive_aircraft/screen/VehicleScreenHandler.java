package immersive_aircraft.screen;

import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.item.UpgradeItem;
import immersive_aircraft.item.WeaponItem;
import immersive_aircraft.screen.slot.FuelSlot;
import immersive_aircraft.screen.slot.TypedSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.DyeItem;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class VehicleScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    private final InventoryVehicleEntity vehicle;

    public VehicleScreenHandler(int syncId, PlayerInventory playerInventory, InventoryVehicleEntity vehicle) {
        super(null, syncId);

        this.vehicle = vehicle;
        this.inventory = vehicle.getInventory();

        inventory.onOpen(playerInventory.player);

        int titleHeight = 10;

        // Vehicle inventory
        for (VehicleInventoryDescription.Slot slot : this.vehicle.getInventoryDescription().getSlots()) {
            if (slot.type == VehicleInventoryDescription.SlotType.BOILER) {
                this.addSlot(new FuelSlot(inventory, slot.index, slot.x, slot.y + titleHeight));
            } else if (slot.type == VehicleInventoryDescription.SlotType.WEAPON) {
                this.addSlot(new TypedSlot(WeaponItem.class, 64, inventory, slot.index, slot.x, slot.y + titleHeight));
            } else if (slot.type == VehicleInventoryDescription.SlotType.UPGRADE) {
                this.addSlot(new TypedSlot(UpgradeItem.class, 1, inventory, slot.index, slot.x, slot.y + titleHeight));
            } else if (slot.type == VehicleInventoryDescription.SlotType.BOOSTER) {
                this.addSlot(new TypedSlot(FireworkRocketItem.class, 1, inventory, slot.index, slot.x, slot.y + titleHeight));
            } else if (slot.type == VehicleInventoryDescription.SlotType.BANNER) {
                this.addSlot(new TypedSlot(BannerItem.class, 1, inventory, slot.index, slot.x, slot.y + titleHeight));
            } else if (slot.type == VehicleInventoryDescription.SlotType.DYE) {
                this.addSlot(new TypedSlot(DyeItem.class, 1, inventory, slot.index, slot.x, slot.y + titleHeight));
            } else {
                this.addSlot(new Slot(inventory, slot.index, slot.x, slot.y + titleHeight));
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

    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (index < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    public void close(PlayerEntity player) {
        super.close(player);
        this.inventory.onClose(player);
    }

    public InventoryVehicleEntity getVehicle() {
        return vehicle;
    }
}
