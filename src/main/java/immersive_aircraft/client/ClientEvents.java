package immersive_aircraft.client;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.network.c2s.CommandMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Client-side event subscribers replacing the 1.16 mixins and fabric callbacks:
 *  - ClientTickEvent drives the raw-state key bindings (KeyBindingMixin)
 *  - RenderGameOverlayEvent draws the aircraft HUD (InGameHudMixin)
 *  - EntityViewRenderEvent.CameraSetup rotates the camera (GameRendererMixin)
 */
@Mod.EventBusSubscriber(modid = Main.MOD_ID, value = Side.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            // 1.16 did this in ClientPlayerEntityMixin.openRidingInventory: riding an
            // InventoryVehicleEntity turns the inventory key into a vehicle-inventory request.
            // NOTE(deviation): the vanilla player inventory will open as well; there is no
            // clean way to suppress it without hacking GameSettings.
            Minecraft client = Minecraft.getMinecraft();
            if (client.player != null && client.currentScreen == null
                    && client.player.getRidingEntity() instanceof InventoryVehicleEntity
                    && client.gameSettings.keyBindInventory.isPressed()) {
                Vec3d velocity = new Vec3d(client.player.motionX, client.player.motionY, client.player.motionZ);
                NetworkHandler.sendToServer(new CommandMessage(CommandMessage.Key.INVENTORY, velocity));
            }
        } else {
            KeyBindings.update();

            Minecraft client = Minecraft.getMinecraft();
            if (client.player != null && client.player.getRidingEntity() instanceof InventoryVehicleEntity) {
                InventoryVehicleEntity vehicle = (InventoryVehicleEntity) client.player.getRidingEntity();

                // Switch to first person when scoping (1.20.1 ClientMain logic)
                if (vehicle.isScoping() != isZooming) {
                    isZooming = vehicle.isScoping();
                    if (isZooming) {
                        perspectiveBeforeZoom = client.gameSettings.thirdPersonView;
                        client.gameSettings.thirdPersonView = 0;
                    } else {
                        client.gameSettings.thirdPersonView = perspectiveBeforeZoom;
                    }
                }

                // Fire weapons when in a vehicle
                activeTicks++;
                if (activeTicks > 20 && KeyBindings.use.isPressed() && client.player.getHeldItemMainhand().isEmpty()) {
                    vehicle.clientFireWeapons(client.player);
                }
            } else {
                activeTicks = 0;
                if (isZooming) {
                    isZooming = false;
                    client.gameSettings.thirdPersonView = perspectiveBeforeZoom;
                }
            }
        }
    }

    private static int activeTicks = 0;
    private static boolean isZooming = false;
    private static int perspectiveBeforeZoom = 0;

    @SubscribeEvent
    public static void onFovModifier(EntityViewRenderEvent.FOVModifier event) {
        Minecraft client = Minecraft.getMinecraft();
        if (client.player != null && client.player.getRidingEntity() instanceof InventoryVehicleEntity) {
            InventoryVehicleEntity vehicle = (InventoryVehicleEntity) client.player.getRidingEntity();
            if (vehicle.isScoping() && vehicle.getZoom() > 0) {
                event.setFOV((float) (event.getFOV() / vehicle.getZoom()));
            }
        }
    }

    @SubscribeEvent
    public static void renderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            OverlayRenderer.renderOverlay(event.getResolution(), event.getPartialTicks());
        }
    }

    @SubscribeEvent
    public static void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        Minecraft client = Minecraft.getMinecraft();
        Entity entity = client.getRenderViewEntity();
        if (client.gameSettings.thirdPersonView == 0 && entity != null && entity.getLowestRidingEntity() instanceof AircraftEntity) {
            AircraftEntity aircraft = (AircraftEntity) entity.getLowestRidingEntity();
            float tickDelta = (float) event.getRenderPartialTicks();

            // rotate camera
            event.setRoll(event.getRoll() + aircraft.getRoll(tickDelta));
            event.setPitch(event.getPitch() + aircraft.getPitch(tickDelta));

            // TODO(port): the 1.16 GameRendererMixin additionally transformed the eye offset
            //  to match the aircraft rotation; Forge's CameraSetup event cannot move the
            //  camera position, so that part is not replicated.
        }
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        for (Item item : Registration.ITEMS) {
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }
}
