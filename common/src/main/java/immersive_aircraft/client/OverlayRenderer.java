package immersive_aircraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import immersive_aircraft.Main;
import immersive_aircraft.client.hud.*;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.BiplaneEntity;
import immersive_aircraft.entity.EngineVehicle;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.WarshipEntity;
import immersive_aircraft.item.upgrade.VehicleStat;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;

import java.util.stream.IntStream;

public class OverlayRenderer {
    public static final OverlayRenderer INSTANCE = new OverlayRenderer();

    private static final ResourceLocation ENGINE_TEX = Main.locate("textures/gui/engine.png");
    private static final ResourceLocation POWER_TEX = Main.locate("textures/gui/power.png");
    private static final ResourceLocation ICONS_TEX = Main.locate("textures/gui/icons.png");

    private float bootUp = 0.0f;
    private float lastTime = 0.0f;

    public int tk = 0;

    public static int renderOverlay(GuiGraphics context, float tickDelta, int barHeightOffset) {
        Minecraft client = Minecraft.getInstance();
        if (client.gameMode != null && client.player != null) {
            if (INSTANCE.tk == 60) INSTANCE.tk = 0;
            if (Config.getInstance().showHotbarEngineGauge && client.player.getRootVehicle() instanceof EngineVehicle aircraft) {
                INSTANCE.renderAircraftGui(client, context, tickDelta, aircraft);
            }
            if (client.player.getRootVehicle() instanceof EngineVehicle aircraft) {
                if (aircraft.getProperties().get(VehicleStat.HUD) == 0 || aircraft.getProperties().get(VehicleStat.DIALS) == 0) {
                    for (Indicator i : new Indicator[]{SpeedIndicator.INSTANCE, AltIndicator.INSTANCE, AzimuthIndicator.INSTANCE,
                            AttitudeIndicator.INSTANCE, VectorIndicator.INSTANCE, WarningIndicator.INSTANCE})
                        i.update(client, aircraft);
                    if (aircraft.getProperties().get(VehicleStat.HUD) == 0
                            && (aircraft instanceof BiplaneEntity || aircraft instanceof WarshipEntity)
                    )   // hud currently supports biplane / warship
                        INSTANCE.renderAircraftHUD(client, context, tickDelta, barHeightOffset, aircraft);
                    if (aircraft.getProperties().get(VehicleStat.DIALS) == 0)   // dials can be used on any aircraft
                        INSTANCE.renderAircraftDials(client, context, tickDelta, barHeightOffset, aircraft);
                }
            }
            if (client.player.getRootVehicle() instanceof VehicleEntity vehicle) {
                INSTANCE.renderAircraftHealth(client, context, vehicle, barHeightOffset);
                INSTANCE.tk++;
                return 10;
            }
            INSTANCE.tk++;
        }
        return 0;
    }

    private void renderAircraftHealth(Minecraft minecraft, GuiGraphics context, VehicleEntity vehicle, int barHeightOffset) {
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int maxHearts = 10;
        int health = (int) Math.ceil(vehicle.getHealth() * maxHearts * 2);

        int y = screenHeight - barHeightOffset - Config.getInstance().healthBarRow * 10;
        int ox = screenWidth / 2 + 91;
        for (int i = 0; i < maxHearts; i++) {
            int u = 52;
            int x = ox - i * 8 - 9;
            context.blit(ICONS_TEX, x, y, u, 9, 9, 9, 64, 64);
            if (i * 2 + 1 < health) {
                context.blit(ICONS_TEX, x, y, 0, 0, 9, 9, 64, 64);
            }
            if (i * 2 + 1 != health) continue;
            context.blit(ICONS_TEX, x, y, 10, 0, 9, 9, 64, 64);
        }
    }

    private void renderAircraftGui(Minecraft client, GuiGraphics context, float tickDelta, EngineVehicle aircraft) {
        assert client.level != null;

        if (aircraft.getGuiStyle() == EngineVehicle.GUI_STYLE.ENGINE) {
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
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private void renderAircraftHUD(Minecraft client, GuiGraphics context, float tickDelta, int barHeightOffset, EngineVehicle aircraft) {
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        LocalPlayer player = client.player;
        int xC = screenWidth / 2;
        int yC = screenHeight / 2;
        int sqD = Math.min(screenWidth * 4 / 10, screenHeight * 4 / 10);
        int rcX = xC;
        int rcY = yC;
        int rc2X = xC;
        int rc2Y = yC;
        int alpha = 255;
        float relXR = 0;
        float relYR = 0;
        float acYR = aircraft.yRotO;
        float acZR = aircraft.roll;
        boolean fullhud = false;
        if (player != null) {
            fullhud = client.options.getCameraType() == CameraType.FIRST_PERSON;
            float playerXR = player.xRotO;
            float playerYR = player.yRotO;
            relXR = playerXR - 10;
            relYR = Math.abs(playerYR - acYR) < Math.abs(360 - (playerYR - acYR)) ? playerYR - acYR : 360 - (playerYR - acYR);
            float xDt = Math.min(Math.max(0.0f, Math.abs(relXR) - 5.0f) / 25.0f, 1.0f);
            float yDt = Math.min(Math.max(0.0f, Math.abs(relYR) - 5.0f) / 25.0f, 1.0f);
            alpha *= Math.min(1.0f - xDt, 1.0f - yDt);    // XY: +-30deg=fade, +-5deg=solid
            rcY -= (relXR / 30.0f) * sqD * 0.8f;  // HUD Y pos
            rcY -= sqD * 3 / 16;  // HUD Y correction
            rcY += (Math.abs(relYR) / 30.0f) * sqD * 0.1f;  // YRot correction
            rcY += acZR * relYR * sqD * 0.0005f;    // roll-YRot correction
            rcX -= (relYR / 30.0f) * sqD * 0.9f;    // HUD X pos
            rcX -= acZR * sqD * 0.01f;    // HUD X correction

            rc2Y -= (relXR / 30.0f) * sqD * 0.4f;  // HUD Y pos
            rc2Y -= sqD * 4 / 16;  // HUD Y correction
            rc2Y += (Math.abs(relYR) / 30.0f) * sqD * 0.1f;  // YRot correction
            rc2Y += acZR * relYR * sqD * 0.0005f;    // roll-YRot correction
            rc2X -= (relYR / 30.0f) * sqD * 0.5f;    // HUD X pos
            rc2X -= acZR * sqD * 0.01f;    // HUD X correction
        }


        if (fullhud) {
            // Draw glasspane
            int color = ARGB32.color((int) Math.round(alpha * 0.8), 255, 255, 255);
            int[] p = vp(rcX, rcY, sqD / 2, (float) Math.toRadians(relXR + 15), (float) Math.toRadians(relYR));
            renderLine(context, p[0], p[1], p[2], p[3], color);    // H1
            renderLine(context, p[0], p[1], p[4], p[5], color);    // V1
            renderLine(context, p[4], p[5], p[6], p[7], color);    // H2
            renderLine(context, p[2], p[3], p[6], p[7], color);    // V2
            int[] edge = new int[]{
                    (p[1] + p[3]) / 2,   // H1
                    (p[5] + p[7]) / 2,   // H2
                    (p[0] + p[4]) / 2,   // V1
                    (p[2] + p[6]) / 2,   // V2
            };
            alpha = (int) Math.max(Math.min(alpha, 255 * 0.8f), 255 * 0.4f);
            if (aircraft.mslWarning > 0 && (Math.floorDiv(tk, 6) & 1) != 0) alpha = alpha * 6 / 5;
            if (aircraft.getEnginePower() <= 0.05f) alpha *= 0.5f;  // the hud is powered by engine
            color = ARGB32.color(alpha, 239, 195, 134);
            SpeedIndicator.INSTANCE.drawHUD(context, client, rc2X - sqD * 6 / 16 + 2, rc2Y, sqD * 5 / 6, aircraft, color, edge);
            AltIndicator.INSTANCE.drawHUD(context, client, rc2X + sqD * 6 / 16 + 2, rc2Y, sqD * 5 / 6, aircraft, color, edge);
            AzimuthIndicator.INSTANCE.drawHUD(context, client, rc2X, rc2Y - sqD * 6 / 16, sqD * 5 / 6, aircraft, color, edge);
            AttitudeIndicator.INSTANCE.drawHUD(context, client, rc2X, rc2Y, sqD * 5 / 6, aircraft, color, edge);
            VectorIndicator.INSTANCE.drawHUD(context, client, rc2X, rc2Y, sqD * 5 / 6, aircraft, color, edge);
            WarningIndicator.INSTANCE.drawHUD(context, client, rc2X, rc2Y + sqD * 3 / 16, sqD * 5 / 6, aircraft, color, edge);
        } else {
            // MiniHUD
            int color = ARGB32.color(255, 239, 195, 134);
            int dashY = screenHeight - barHeightOffset - Config.getInstance().healthBarRow * 10 - 10;
            for (Indicator i : new Indicator[]{
                    SpeedIndicator.INSTANCE,
                    AltIndicator.INSTANCE,
                    AzimuthIndicator.INSTANCE,
                    WarningIndicator.INSTANCE,
            }) i.drawDashboard(context, client, xC, dashY, aircraft, color);
        }
    }

    private void renderAircraftDials(Minecraft client, GuiGraphics context, float tickDelta, int barHeightOffset, EngineVehicle aircraft) {
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int scale = Math.max(1, Math.min(screenWidth / 240, screenHeight / 240));
        SpeedIndicator.INSTANCE.drawDials(context, client, screenWidth - 84 * scale, screenHeight - 28 * scale, scale,  aircraft);
        AltIndicator.INSTANCE.drawDials(context, client, screenWidth - 28 * scale, screenHeight - 28 * scale, scale, aircraft);
        AzimuthIndicator.INSTANCE.drawDials(context, client, screenWidth - 56 * scale, screenHeight - 120 * scale, scale, aircraft);
        AttitudeIndicator.INSTANCE.drawDials(context, client, screenWidth - 84 * scale, screenHeight - 84 * scale, scale, aircraft);
        VectorIndicator.INSTANCE.drawDials(context, client, screenWidth - 28 * scale, screenHeight - (56 * scale + 28 * Math.min(2, scale)), scale, aircraft);
        WarningIndicator.INSTANCE.drawDials(context, client, screenWidth - (112 * scale + 14), screenHeight - 88 * scale, scale, aircraft);
    }

    public static void renderLine(GuiGraphics context, int x01, int y01, int x02, int y02, int color) {
        renderLine(context, x01, y01, x02, y02, color, false);
    }

    public static void renderLine(GuiGraphics context, int x01, int y01, int x02, int y02, int color, boolean dotLine) {
        renderLine(context, x01, y01, x02, y02, color, dotLine, false);
    }

    public static void renderLine(GuiGraphics context, int x01, int y01, int x02, int y02, int color, boolean dotLine, boolean dropShadow) {
        if (Math.abs(y02 - y01) > Math.abs(x02 - x01)) {
            int y1 = Math.min(y01, y02),
                    y2 = Math.max(y01, y02),
                    x1 = y02 > y01 ? x01 : x02,
                    x2 = y02 > y01 ? x02 : x01;
            if (x1 == x2 && !dotLine) {
                if (dropShadow) context.fill(x01 + 1, y01 + 1, x01 + 2, y02 + 1, ARGB32.color(127, 0, 0, 0));
                context.fill(x01, y01, x01 + 1, y02 + 1, color);
                return;
            }
            var it = IntStream.range(y1, y2 + 1).iterator();
            while (it.hasNext()) {
                int py = it.nextInt();
                int dx = x1 + (int) ((x2 - x1) * ((py - y1) / (float) (y2 - y1)));
                if (!dotLine || (py & 1) != 0) {
                    if (dropShadow) context.fill(dx + 1, py + 1, dx + 2, py + 2, ARGB32.color(127, 0, 0, 0));
                    context.fill(dx, py, dx + 1, py + 1, color);
                }
            }
        } else {
            int x1 = Math.min(x01, x02),
                    x2 = Math.max(x01, x02),
                    y1 = x02 > x01 ? y01 : y02,
                    y2 = x02 > x01 ? y02 : y01;
            if (y1 == y2 && !dotLine) {
                if (dropShadow) context.fill(x01 + 1, y01 + 1, x02 + 1, y01 + 2, ARGB32.color(127, 0, 0, 0));
                context.fill(x01, y01, x02 + 1, y01 + 1, color);
                return;
            }
            var it = IntStream.range(x1, x2 + 1).iterator();
            while (it.hasNext()) {
                int px = it.nextInt();
                int dy = y1 + (int) ((y2 - y1) * ((px - x1) / (float) (x2 - x1)));
                if (!dotLine || (px & 1) != 0) {
                    if (dropShadow) context.fill(px + 1, dy + 1, px + 2, dy + 2, ARGB32.color(127, 0, 0, 0));
                    context.fill(px, dy, px + 1, dy + 1, color);
                }
            }
        }
    }

    private static int[] vp(int ix, int iy, int r, float rx, float ry) {
        int x1 = -r, y1 = -r;   // p1
        int x2 = r, y2 = -r;   // p2
        int x3 = -r, y3 = r;   // p3
        int x4 = r, y4 = r;   // p4
        x1 *= Math.cos(ry);
        x2 *= Math.cos(ry);
        x3 *= Math.cos(ry);
        x4 *= Math.cos(ry);
        y1 *= Math.cos(rx);
        y2 *= Math.cos(rx);
        y3 *= Math.cos(rx);
        y4 *= Math.cos(rx);
        float x11 = Math.abs(rx) * (float) Math.sin(rx) * r / 1.8f;
        x1 -= x11;
        x2 += x11;
        x3 += x11;
        x4 -= x11;
        float y11 = Math.abs(ry) * (float) Math.sin(ry) * r / 1.8f;
        y1 -= y11;
        y2 += y11;
        y3 += y11;
        y4 -= y11;
        return new int[]{ix + x1, iy + y1, ix + x2, iy + y2, ix + x3, iy + y3, ix + x4, iy + y4};
    }

    public static void drawScrew(GuiGraphics context, int x, int y, int scale, boolean r, int color) {
        renderLine(context, x - scale, y - scale * 2, x + scale, y - scale * 2, color);
        renderLine(context, x - scale * 2, y - scale, x - scale * 2, y + scale, color);
        renderLine(context, x - scale, y + scale * 2, x + scale, y + scale * 2, color);
        renderLine(context, x + scale * 2, y - scale, x + scale * 2, y + scale, color);
        if (r)
            renderLine(context, x - scale, y - scale, x + scale, y + scale, color);
        else
            renderLine(context, x + scale, y - scale, x - scale, y + scale, color);
    }
}
