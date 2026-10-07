package immersive_aircraft.fabric.cobalt.registration;

import immersive_aircraft.cobalt.registration.CobaltFuelRegistry;
import immersive_aircraft.util.Utils;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;

public class CobaltFuelRegistryImpl extends CobaltFuelRegistry {
    private static volatile FuelValues fuelValues;

    public CobaltFuelRegistryImpl() {
        INSTANCE = this;
    }

    public static void setFuelValues(FuelValues values) {
        fuelValues = values;
    }

    @Override
    public int get(ItemStack stack) {
        FuelValues values = fuelValues;
        return values == null ? 0 : values.burnDuration(stack);
    }

    @Override
    public int getFluidFuelTime(ItemStack stack) {
        return refuelFluid(stack.copy(), ContainerItemContext.withConstant(stack), true);
    }

    @Override
    public int refuelFluid(Container inventory, int slot) {
        int time = refuelFluid(inventory.getItem(slot), ContainerItemContext.ofSingleSlot(InventoryStorage.of(inventory, null).getSlot(slot)), false);
        if (time > 0) {
            inventory.setChanged();
        }
        return time;
    }

    private int refuelFluid(ItemStack stack, ContainerItemContext context, boolean simulate) {
        Storage<FluidVariant> storage = FluidStorage.ITEM.find(stack, context);
        if (storage == null) {
            return 0;
        }
        for (var view : storage.nonEmptyViews()) {
            FluidVariant fluid = view.getResource();
            int time = Utils.getFluidFuelTime(fluid.getFluid());
            if (time > 0) {
                try (Transaction transaction = Transaction.openNested(Transaction.getCurrentUnsafe())) {
                    if (storage.extract(fluid, FluidConstants.BUCKET, transaction) == FluidConstants.BUCKET) {
                        if (!simulate) {
                            transaction.commit();
                        }
                        return time;
                    }
                }
            }
        }
        return 0;
    }
}
