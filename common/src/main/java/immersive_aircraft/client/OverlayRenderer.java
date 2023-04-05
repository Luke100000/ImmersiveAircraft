package immersive_aircraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import immersive_aircraft.Main;
import immersive_aircraft.entity.EngineAircraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class OverlayRenderer {
    private static final Identifier TEXTURE = Main.locate("textures/engine.png");
    private static final Identifier TEXTURE2 = Main.locate("textures/power.png");

    private static float bootUp = 0.0f;
    private static float lastTime = 0;

    public static void renderOverlay(MatrixStack matrices, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.options.hudHidden && client.interactionManager != null) {
            if (client.player != null && client.player.getRootVehicle() instanceof EngineAircraft) {
                EngineAircraft aircraft = (EngineAircraft)client.player.getRootVehicle();
                renderAircraftGui(client, matrices, tickDelta, aircraft);
            }
        }
    }

    private static void renderAircraftGui(MinecraftClient client, MatrixStack matrices, float tickDelta, EngineAircraft aircraft) {
        assert client.world != null;

        if (aircraft.getGuiStyle() == EngineAircraft.GUI_STYLE.ENGINE) {
            float time = client.world.getTime() % 65536 + tickDelta;
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

            int x = client.getWindow().getScaledWidth() / 2;
            int y = client.getWindow().getScaledHeight() - 37;

            if (client.interactionManager != null && !client.interactionManager.hasExperienceBar()) {
                y += 7;
            }

            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            client.getTextureManager().bindTexture(TEXTURE);
            DrawableHelper.drawTexture(matrices, x - 9, y - 9, (frame % 5) * 18, Math.floorDiv(frame, 5) * 18, 18, 18, 90, 90);

            client.getTextureManager().bindTexture(TEXTURE2);
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            DrawableHelper.drawTexture(matrices, x - 9, y - 9, (powerFrame % 5) * 18, Math.floorDiv(powerFrame, 5) * 18, 18, 18, 90, 90);
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 0.5f);
            DrawableHelper.drawTexture(matrices, x - 9, y - 9, (powerFrameTarget % 5) * 18, Math.floorDiv(powerFrameTarget, 5) * 18, 18, 18, 90, 90);
        }
    }
}
