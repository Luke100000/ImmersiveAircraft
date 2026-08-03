package immersive_aircraft.client;

import immersive_aircraft.Main;
import immersive_aircraft.entity.EngineAircraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

/**
 * 1.12.2 port: MatrixStack/DrawableHelper replaced by Gui.drawModalRectWithCustomSizedTexture
 * and GlStateManager.color. Invoked from {@link ClientEvents} on RenderGameOverlayEvent.Post.
 */
public class OverlayRenderer {
    private static final ResourceLocation TEXTURE = Main.locate("textures/engine.png");
    private static final ResourceLocation TEXTURE2 = Main.locate("textures/power.png");

    private static float bootUp = 0.0f;
    private static float lastTime = 0;

    public static void renderOverlay(ScaledResolution resolution, float tickDelta) {
        Minecraft client = Minecraft.getMinecraft();
        if (!client.gameSettings.hideGUI && client.playerController != null) {
            if (client.player != null && client.player.getLowestRidingEntity() instanceof EngineAircraft) {
                EngineAircraft aircraft = (EngineAircraft)client.player.getLowestRidingEntity();
                renderAircraftGui(client, resolution, tickDelta, aircraft);
            }
        }
    }

    private static void renderAircraftGui(Minecraft client, ScaledResolution resolution, float tickDelta, EngineAircraft aircraft) {
        if (client.world == null) {
            return;
        }

        if (aircraft.getGuiStyle() == EngineAircraft.GUI_STYLE.ENGINE) {
            float time = client.world.getWorldTime() % 65536 + tickDelta;
            float delta = time - lastTime;
            lastTime = time;

            // boot-up animation
            int frame;
            if (aircraft.getEngineTarget() > 0 && aircraft.getEnginePower() > 0.001) {
                if (bootUp < 1.0f) {
                    bootUp = Math.min(1.0f, bootUp + delta * 0.2f);
                    frame = (int)(bootUp * 5);
                } else {
                    int FPS = 30;
                    int animation = (int)(aircraft.engineRotation.getSmooth(tickDelta) / 20.0f * FPS);
                    frame = 5 + animation % 6;
                }
            } else {
                if (bootUp > 0.0f) {
                    bootUp = Math.max(0.0f, bootUp - delta * 0.1f);
                    frame = 10 + (int)((1.0 - bootUp) * 10);
                } else {
                    frame = 20;
                }
            }

            int powerFrame = (int)((1.0f - aircraft.getEnginePower()) * 10 + 10.5);
            int powerFrameTarget = (int)((1.0f - aircraft.getEngineTarget()) * 10 + 10.5);

            int x = resolution.getScaledWidth() / 2;
            int y = resolution.getScaledHeight() - 37;

            if (client.playerController != null && !client.playerController.gameIsSurvivalOrAdventure()) {
                y += 7;
            }

            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            client.getTextureManager().bindTexture(TEXTURE);
            Gui.drawModalRectWithCustomSizedTexture(x - 9, y - 9, (frame % 5) * 18, Math.floorDiv(frame, 5) * 18, 18, 18, 90, 90);

            client.getTextureManager().bindTexture(TEXTURE2);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            Gui.drawModalRectWithCustomSizedTexture(x - 9, y - 9, (powerFrame % 5) * 18, Math.floorDiv(powerFrame, 5) * 18, 18, 18, 90, 90);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 0.5f);
            Gui.drawModalRectWithCustomSizedTexture(x - 9, y - 9, (powerFrameTarget % 5) * 18, Math.floorDiv(powerFrameTarget, 5) * 18, 18, 18, 90, 90);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}
