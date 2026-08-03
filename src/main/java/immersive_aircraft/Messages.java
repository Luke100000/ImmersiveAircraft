package immersive_aircraft;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.network.c2s.CollisionMessage;
import immersive_aircraft.network.c2s.CommandMessage;
import immersive_aircraft.network.c2s.EnginePowerMessage;
import immersive_aircraft.network.c2s.RequestInventory;
import immersive_aircraft.network.s2c.InventoryUpdateMessage;
import net.minecraftforge.fml.relauncher.Side;

public class Messages {
    public static void loadMessages() {
        NetworkHandler.registerMessage(EnginePowerMessage.class, Side.SERVER);
        NetworkHandler.registerMessage(CommandMessage.class, Side.SERVER);
        NetworkHandler.registerMessage(RequestInventory.class, Side.SERVER);
        NetworkHandler.registerMessage(CollisionMessage.class, Side.SERVER);
        NetworkHandler.registerMessage(immersive_aircraft.network.c2s.FireMessage.class, Side.SERVER);
        NetworkHandler.registerMessage(InventoryUpdateMessage.class, Side.CLIENT);
        NetworkHandler.registerMessage(immersive_aircraft.network.s2c.FireResponse.class, Side.CLIENT);
    }
}
