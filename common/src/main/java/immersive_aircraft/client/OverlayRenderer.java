package immersive_aircraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import immersive_aircraft.Main;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.EngineAircraft;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class OverlayRenderer {
    static final OverlayRenderer INSTANCE = new OverlayRenderer();

    private static final ResourceLocation ENGINE_TEX = Main.locate("textures/gui/engine.png");
    private static final ResourceLocation POWER_TEX = Main.locate("textures/gui/power.png");
    private static final ResourceLocation ICONS_TEX = Main.locate("textures/gui/icons.png");

    private float bootUp = 0.0f;
    private float lastTime = 0.0f;

    public static void renderOverlay(GuiGraphics context, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        if (!client.options.hideGui && client.gameMode != null && client.player != null) {
            if (Config.getInstance().showHotbarEngineGauge && client.player.getRootVehicle() instanceof EngineAircraft aircraft) {
                INSTANCE.renderAircraftGui(client, context, tickDelta, aircraft);
            }
            if (client.player.getRootVehicle() instanceof VehicleEntity vehicle) {
                INSTANCE.renderAircraftHealth(client, context, vehicle);
            }
        }
    }

    private static int getVehicleMaxHearts(VehicleEntity vehicle) {
        return (int) Math.ceil(10.0 / vehicle.getDurability());
    }

    private void renderAircraftHealth(Minecraft minecraft, GuiGraphics context, VehicleEntity vehicle) {
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int maxHearts = getVehicleMaxHearts(vehicle);
        int health = (int) Math.ceil(vehicle.getHealth() * maxHearts * 2);

        int y = screenHeight - 49 - Config.getInstance().healthBarRow * 10;
        int ox = screenWidth / 2 + 91;
        int hearts = 0;
        while (maxHearts > 0) {
            int heartsInRow = Math.min(maxHearts, 10);
            maxHearts -= heartsInRow;
            for (int i = 0; i < heartsInRow; i++) {
                int u = 52;
                int x = ox - i * 8 - 9;
                context.blit(ICONS_TEX, x, y, u, 9, 9, 9, 64, 64);
                if (i * 2 + 1 + hearts < health) {
                    context.blit(ICONS_TEX, x, y, 0, 0, 9, 9, 64, 64);
                }
                if (i * 2 + 1 + hearts != health) continue;
                context.blit(ICONS_TEX, x, y, 10, 0, 9, 9, 64, 64);
            }
            y -= 10;
            hearts += 20;
        }
    }

    private void renderAircraftGui(Minecraft client, GuiGraphics context, float tickDelta, EngineAircraft aircraft) {
        assert client.level != null;

        if (aircraft.getGuiStyle() == EngineAircraft.GUI_STYLE.ENGINE) {
            float time = client.level.getGameTime() % 65536 + tickDelta;
            float delta = time - lastTime;
            lastTime = time;

            // boot-up animation
            int frame;
            if (aircraft.getEngineTarget() > 0 && aircraft.getEnginePower() > 0.001) {
                if (bootUp < 1.0f) {
                    bootUp = Math.min(1.0f, bootUp + delta * 0.2f);
                    frame = (int) (bootUp * 5);
                } else {
                    final int FPS = 30;
                    int animation = (int) (aircraft.engineRotation.getSmooth(tickDelta) / 20.0f * FPS);
                    frame = 5 + animation % 6;
                }
            } else {
                if (bootUp > 0.0f) {
                    bootUp = Math.max(0.0f, bootUp - delta * 0.1f);
                    frame = 10 + (int) ((1.0 - bootUp) * 10);
                } else {
                    frame = 20;
                }
            }

            int powerFrame = (int) ((1.0f - aircraft.getEnginePower()) * 10 + 10.5);
            int powerFrameTarget = (int) ((1.0f - aircraft.getEngineTarget()) * 10 + 10.5);

            int x = client.getWindow().getGuiScaledWidth() / 2;
            int y = client.getWindow().getGuiScaledHeight() - 37;

            if (client.gameMode != null && !client.gameMode.hasExperience()) {
                y += 7;
            }

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            context.blit(ENGINE_TEX, x - 9, y - 9, (frame % 5) * 18, Math.floorDiv(frame, 5) * 18, 18, 18, 90, 90);

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.enableBlend();
            context.blit(POWER_TEX, x - 9, y - 9, (powerFrame % 5) * 18, Math.floorDiv(powerFrame, 5) * 18, 18, 18, 90, 90);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.5f);
            context.blit(POWER_TEX, x - 9, y - 9, (powerFrameTarget % 5) * 18, Math.floorDiv(powerFrameTarget, 5) * 18, 18, 18, 90, 90);
        }
    }
}
