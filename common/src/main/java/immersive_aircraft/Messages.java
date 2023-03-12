package immersive_aircraft;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.network.c2s.CommandMessage;
import immersive_aircraft.network.c2s.EnginePowerMessage;
import immersive_aircraft.network.s2c.OpenGuiRequest;

public class Messages {
    public static void loadMessages() {
        NetworkHandler.registerMessage(EnginePowerMessage.class);
        NetworkHandler.registerMessage(CommandMessage.class);
        NetworkHandler.registerMessage(OpenGuiRequest.class);
    }
}
