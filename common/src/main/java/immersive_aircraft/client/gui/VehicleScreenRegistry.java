package immersive_aircraft.client.gui;

import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.network.s2c.OpenGuiRequest;
import immersive_aircraft.screen.VehicleScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.HashMap;

public class VehicleScreenRegistry {
    public static HashMap<Class<?>, OpenGuiRequestHandler> GUI_OPEN_HANDLERS = new HashMap<>();

    public static void register(Class<?> clazz, OpenGuiRequestHandler handler) {
        GUI_OPEN_HANDLERS.put(clazz, handler);
    }

    public static final OpenGuiRequestHandler DEFAULT = (vehicle, player, message) -> {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.player != null) {
            VehicleScreenHandler handler = (VehicleScreenHandler) vehicle.createMenu(message.getSyncId(), client.player.getInventory(), client.player);
            VehicleScreen screen = new VehicleScreen(handler, client.player.getInventory(), vehicle.getDisplayName());
            client.player.containerMenu = screen.getMenu();
            client.setScreen(screen);
        }
    };

    static {
        // Example of registering a custom handler
        GUI_OPEN_HANDLERS.put(InventoryVehicleEntity.class, DEFAULT);
    }

    public interface OpenGuiRequestHandler {
        void handle(InventoryVehicleEntity vehicle, LocalPlayer player, OpenGuiRequest message);
    }
}
