package immersive_aircraft;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.network.c2s.CommandMessage;
import immersive_aircraft.network.c2s.EnginePowerMessage;

public class Messages {
    public static void bootstrap() {
        NetworkHandler.registerMessage(EnginePowerMessage.class);
        NetworkHandler.registerMessage(CommandMessage.class);
    }
}
