package immersive_aircraft.network;

import immersive_aircraft.client.gui.VehicleScreen;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.network.s2c.InventoryUpdateMessage;
import immersive_aircraft.network.s2c.OpenGuiRequest;
import immersive_aircraft.screen.VehicleScreenHandler;
import net.minecraft.client.MinecraftClient;

public class ClientNetworkManager implements NetworkManager {
    @Override
    public void handleOpenGuiRequest(OpenGuiRequest message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.player != null) {
            InventoryVehicleEntity vehicle = (InventoryVehicleEntity)client.world.getEntityById(message.getVehicle());
            if (vehicle != null) {
                VehicleScreenHandler handler = (VehicleScreenHandler)vehicle.createMenu(message.getSyncId(), client.player.getInventory(), client.player);
                VehicleScreen screen = new VehicleScreen(handler, client.player.getInventory(), vehicle.getDisplayName());
                client.player.currentScreenHandler = screen.getScreenHandler();
                client.setScreen(screen);
            }
        }
    }

    @Override
    public void handleInventoryUpdate(InventoryUpdateMessage message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.player != null) {
            InventoryVehicleEntity vehicle = (InventoryVehicleEntity)client.world.getEntityById(message.getVehicle());
            if (vehicle != null) {
                vehicle.getInventory().setStack(message.getIndex(), message.getStack());
            }
        }
    }
}
