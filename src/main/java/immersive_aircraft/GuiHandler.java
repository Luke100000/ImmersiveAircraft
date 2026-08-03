package immersive_aircraft;

import immersive_aircraft.client.gui.VehicleScreen;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.screen.VehicleScreenHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

/**
 * Single GUI id; the vehicle entity id is passed in the x parameter of openGui.
 */
public class GuiHandler implements IGuiHandler {
    public static final int GUI_VEHICLE = 0;

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == GUI_VEHICLE) {
            Entity entity = world.getEntityByID(x);
            if (entity instanceof InventoryVehicleEntity) {
                return new VehicleScreenHandler(player.inventory, (InventoryVehicleEntity) entity);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == GUI_VEHICLE) {
            Entity entity = world.getEntityByID(x);
            if (entity instanceof InventoryVehicleEntity) {
                return new VehicleScreen(new VehicleScreenHandler(player.inventory, (InventoryVehicleEntity) entity));
            }
        }
        return null;
    }
}
