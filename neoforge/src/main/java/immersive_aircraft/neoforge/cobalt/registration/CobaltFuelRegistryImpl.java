package immersive_aircraft.neoforge.cobalt.registration;

import immersive_aircraft.cobalt.registration.CobaltFuelRegistry;
import immersive_aircraft.util.Utils;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class CobaltFuelRegistryImpl extends CobaltFuelRegistry {
    public CobaltFuelRegistryImpl() {
        INSTANCE = this;
    }

    @Override
    public int get(ItemStack stack) {
        return stack.getBurnTime(null);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.getCraftingRemainingItem();
    }

    @Override
    public int getFluidFuelTime(ItemStack stack) {
        return refuelFluid(stack.copy().getCapability(Capabilities.FluidHandler.ITEM), true);
    }

    @Override
    public int refuelFluid(Container inventory, int slot) {
        IFluidHandlerItem handler = inventory.getItem(slot).copy().getCapability(Capabilities.FluidHandler.ITEM);
        int time = refuelFluid(handler, false);
        if (time > 0) {
            inventory.setItem(slot, handler.getContainer());
        }
        return time;
    }

    private int refuelFluid(IFluidHandlerItem handler, boolean simulate) {
        if (handler == null) {
            return 0;
        }
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            FluidStack fluid = handler.getFluidInTank(tank);
            int time = fluid.isEmpty() ? 0 : Utils.getFluidFuelTime(fluid.getFluid());
            if (time > 0) {
                FluidStack request = fluid.copyWithAmount(1000);
                FluidStack drained = handler.drain(request, IFluidHandler.FluidAction.SIMULATE);
                if (FluidStack.matches(drained, request)) {
                    if (simulate || FluidStack.matches(handler.drain(request, IFluidHandler.FluidAction.EXECUTE), request)) {
                        return time;
                    }
                    return 0;
                }
            }
        }
        return 0;
    }
}
