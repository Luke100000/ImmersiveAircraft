package immersive_aircraft;

import immersive_aircraft.config.Config;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.network.ClientNetworkManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class ClientMain {
    public static void postLoad() {
        //finish the items
        ItemsClient.postLoad();

        Main.networkManager = new ClientNetworkManager();
    }

    private static boolean isInVehicle;
    private static CameraType lastPerspective;

    public static void tick() {
        if (Config.getInstance().separateCamera) {
            Minecraft client = Minecraft.getInstance();
            LocalPlayer player = client.player;
            boolean b = player != null && player.getRootVehicle() instanceof AircraftEntity;
            if (b != isInVehicle) {
                if (lastPerspective == null) {
                    lastPerspective = Config.getInstance().useThirdPersonByDefault ? CameraType.THIRD_PERSON_BACK : CameraType.FIRST_PERSON;
                }

                isInVehicle = b;

                CameraType perspective = client.options.getCameraType();
                client.options.setCameraType(lastPerspective);
                lastPerspective = perspective;
            }
        }
    }
}
