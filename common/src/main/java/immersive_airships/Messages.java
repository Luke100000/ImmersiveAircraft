package immersive_airships;

import immersive_airships.cobalt.network.NetworkHandler;
import immersive_airships.network.c2s.EnginePowerMessage;

public class Messages {
    public static void bootstrap() {
        NetworkHandler.registerMessage(EnginePowerMessage.class);
    }
}
