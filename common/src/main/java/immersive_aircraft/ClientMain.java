package immersive_aircraft;

import immersive_aircraft.client.KeyBindings;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.network.ClientNetworkManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

public class ClientMain {
    private static int activeTicks;

    public static void postLoad() {
        Main.networkManager = new ClientNetworkManager();

        Main.cameraGetter = () -> Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Main.firstPersonGetter = () -> Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;
    }

    private static boolean isZooming;
    private static CameraType perspectiveBeforeZoom;

    private static boolean isInVehicle;
    private static CameraType lastPerspective;

    private static long lastTime;

    public static void tick() {
        Minecraft client = Minecraft.getInstance();

        Main.frameTime = client.getFrameTime();

        // Only tick once per tick
        if (client.level == null || client.level.getGameTime() == lastTime) {
            return;
        }
        lastTime = client.level.getGameTime();

        // Toggle view when entering a vehicle
        if (Config.getInstance().separateCamera) {
            LocalPlayer player = client.player;
            boolean b = player != null && player.getRootVehicle() instanceof VehicleEntity;
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

        if (client.player != null && client.player.getVehicle() instanceof InventoryVehicleEntity vehicle) {
            // Switch to first person when scoping
            if (vehicle.isScoping() != isZooming) {
                isZooming = vehicle.isScoping();
                if (isZooming) {
                    perspectiveBeforeZoom = client.options.getCameraType();
                    client.options.setCameraType(CameraType.FIRST_PERSON);
                } else {
                    client.options.setCameraType(perspectiveBeforeZoom);
                }
            }

            // Fire weapons when in a vehicle
            activeTicks++;

            if (activeTicks > 20 && KeyBindings.use.isDown() && client.player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                vehicle.clientFireWeapons(client.player);
            }
        } else {
            activeTicks = 0;
        }
    }
}
