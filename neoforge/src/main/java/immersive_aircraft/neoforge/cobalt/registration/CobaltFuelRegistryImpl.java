package immersive_aircraft.neoforge.cobalt.registration;

import immersive_aircraft.cobalt.registration.CobaltFuelRegistry;
import immersive_aircraft.util.Utils;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;

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
        return values == null ? 0 : stack.getBurnTime(null, values);
    }

    @Override
    public int getFluidFuelTime(ItemStack stack) {
        return refuelFluid(new SimpleContainer(stack.copy()), 0, true);
    }

    @Override
    public int refuelFluid(Container inventory, int slot) {
        return refuelFluid(inventory, slot, false);
    }

    private int refuelFluid(Container inventory, int slot, boolean simulate) {
        ItemAccess access = ItemAccess.forHandlerIndex(VanillaContainerWrapper.of(inventory), slot);
        ResourceHandler<FluidResource> handler = inventory.getItem(slot).getCapability(Capabilities.Fluid.ITEM, access);
        if (handler == null) {
            return 0;
        }
        for (int tank = 0; tank < handler.size(); tank++) {
            FluidResource fluid = handler.getResource(tank);
            int time = fluid.isEmpty() ? 0 : Utils.getFluidFuelTime(fluid.getFluid());
            if (time > 0) {
                try (Transaction transaction = Transaction.open(Transaction.getCurrentOpenedTransaction())) {
                    if (handler.extract(fluid, FluidType.BUCKET_VOLUME, transaction) == FluidType.BUCKET_VOLUME) {
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
