package immersive_aircraft;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.network.c2s.EnginePowerMessage;

public class Messages {
    public static void bootstrap() {
        NetworkHandler.registerMessage(EnginePowerMessage.class);
    }
}
