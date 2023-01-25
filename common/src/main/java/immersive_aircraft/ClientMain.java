package immersive_aircraft;

import immersive_aircraft.config.Config;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.network.ClientNetworkManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;

public class ClientMain {
    public static void postLoad() {
        //finish the items
        ItemsClient.postLoad();

        Main.networkManager = new ClientNetworkManager();
    }

    private static boolean isInVehicle;
    private static Perspective lastPerspective;

    public static void tick() {
        if (Config.getInstance().separateCamera) {
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;
            boolean b = player != null && player.getRootVehicle() instanceof AircraftEntity;
            if (b != isInVehicle) {
                if (lastPerspective == null) {
                    lastPerspective = Config.getInstance().useThirdPersonByDefault ? Perspective.THIRD_PERSON_BACK : Perspective.FIRST_PERSON;
                }

                isInVehicle = b;

                Perspective perspective = client.options.getPerspective();
                client.options.setPerspective(lastPerspective);
                lastPerspective = perspective;
            }
        }
    }
}
