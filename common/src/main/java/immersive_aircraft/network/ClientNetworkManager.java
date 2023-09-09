package immersive_aircraft.network;

import immersive_aircraft.client.gui.VehicleScreen;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.network.s2c.InventoryUpdateMessage;
import immersive_aircraft.network.s2c.OpenGuiRequest;
import immersive_aircraft.screen.VehicleScreenHandler;
import net.minecraft.client.Minecraft;

public class ClientNetworkManager implements NetworkManager {
    @Override
    public void handleOpenGuiRequest(OpenGuiRequest message) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.player != null) {
            InventoryVehicleEntity vehicle = (InventoryVehicleEntity)client.level.getEntity(message.getVehicle());
            if (vehicle != null) {
                VehicleScreenHandler handler = (VehicleScreenHandler)vehicle.createMenu(message.getSyncId(), client.player.getInventory(), client.player);
                VehicleScreen screen = new VehicleScreen(handler, client.player.getInventory(), vehicle.getDisplayName());
                client.player.containerMenu = screen.getMenu();
                client.setScreen(screen);
            }
        }
    }

    @Override
    public void handleInventoryUpdate(InventoryUpdateMessage message) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.player != null) {
            InventoryVehicleEntity vehicle = (InventoryVehicleEntity)client.level.getEntity(message.getVehicle());
            if (vehicle != null) {
                vehicle.getInventory().setItem(message.getIndex(), message.getStack());
            }
        }
    }
}
