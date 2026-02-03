package immersive_aircraft.client.hud;

import immersive_aircraft.client.OverlayRenderer;
import immersive_aircraft.entity.EngineVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;

import java.util.stream.IntStream;

import static immersive_aircraft.client.hud.Colors.*;

public class VectorIndicator implements Indicator {
    public static final VectorIndicator INSTANCE = new VectorIndicator();
    private float pitchRate = 0;
    private float yawRate = 0;
    private float rollRate = 0;
    private Vec3 lastSpeed = new Vec3(0, 0, 0);
    private Vec3 iSpeed = new Vec3(0, 0, 0);
    private Vec3 iSpeedRt = new Vec3(0, 0, 0);
    private double hAngle = 0;

    @Override
    public void update(Minecraft client, EngineVehicle aircraft) {
        float pitch = aircraft.getXRot();
        pitchRate = (pitch - aircraft.xRotO) * 20;
        float yaw = aircraft.getYRot();
        yawRate = (yaw - aircraft.yRotO) * 20;
        float roll = aircraft.getRoll();
        rollRate = (float) Math.toRadians((roll - aircraft.prevRoll) * 20);
        Vec3 speed = aircraft.getSpeedVector().scale(20.0d);
        if (!speed.equals(lastSpeed)) {
            iSpeed = speed.add(lastSpeed.reverse());
            iSpeedRt = iSpeed.scale(-1 / 30.0f);
            lastSpeed = speed;
        }
        if (iSpeed.add(iSpeedRt).dot(iSpeed) > 0) {
            iSpeed = iSpeed.add(iSpeedRt);
        } else {
            iSpeed = new Vec3(0, 0, 0);
            iSpeedRt = new Vec3(0, 0, 0);
        }
        Vec3 mSpeed = lastSpeed.add(iSpeed.reverse());
        Vec3 vNormal = Vec3.directionFromRotation(0, yaw + 90);
        hAngle = Math.toDegrees(Math.asin(mSpeed.normalize().dot(vNormal)));
    }

    public void drawDashboard(GuiGraphics context, Minecraft client, int baseX, int baseY, EngineVehicle aircraft, int color) {
    }

    @Override
    public void drawHUD(GuiGraphics context, Minecraft client, int baseX, int baseY, int width, EngineVehicle aircraft, int color, int[] edge) {
        if (edgeCheck(edge, 5, baseX, baseY - width / 10)) {
            StringDrawer.drawString5(context, client, "¯W¯", baseX + 1, baseY - width * 30 / 100 + 3, color, false);
        }
        if (edgeCheck(edge, 5, baseX - width * 10 / 100, baseY)) {
            OverlayRenderer.renderLine(context, baseX - width * 15 / 100 - 4, baseY, baseX - width * 5 / 100 - 4, baseY, color);
        }
        if (edgeCheck(edge, 8, baseX - width * 37 / 100, baseY)) {
            OverlayRenderer.renderLine(context, baseX - width * 45 / 100 - 2, baseY, baseX - width * 30 / 100 - 2, baseY, color);
        }
        if (edgeCheck(edge, 5, baseX + width * 10 / 100, baseY)) {
            OverlayRenderer.renderLine(context, baseX + width * 5 / 100 + 2, baseY, baseX + width * 15 / 100 + 2, baseY, color);
        }
        if (edgeCheck(edge, 8, baseX + width * 37 / 100, baseY)) {
            OverlayRenderer.renderLine(context, baseX + width * 30 / 100, baseY, baseX + width * 45 / 100, baseY, color);
        }
        if (edgeCheck(edge, 5, baseX - width * 6 / 100, baseY)) {
            StringDrawer.drawString6(context, client, "▷", baseX - width * 5 / 100, baseY, color, false);
        }
        if (edgeCheck(edge, 5, baseX + width * 6 / 100, baseY)) {
            StringDrawer.drawString4(context, client, "◁", baseX + width * 5 / 100 + 1, baseY, color, false);
        }

        // bank angle scale
        if (edgeCheck(edge, 2, baseX + 1, baseY + width * 45 / 100 - 1)) {
            StringDrawer.drawString2(context, client, "'", baseX + 1, baseY + width * 45 / 100 - 1, color, false);
        }
        IntStream.range(1, 13).forEach(i -> {
            int y = baseY + width * 45 / 100 - (i >= 10 ? 1 : 0);
            if (edgeCheck(edge, 2, baseX + 1 + i * width / 40, y)) {
                StringDrawer.drawString2(context, client, "'", baseX + 1 + i * width / 40, y, color, false);
            }
            if (edgeCheck(edge, 2, baseX + 1 - i * width / 40, y)) {
                StringDrawer.drawString2(context, client, "'", baseX + 1 - i * width / 40, y, color, false);
            }
        });

        if (edgeCheck(edge, 5, baseX + 1 + (int) (rollRate * width / 4), baseY + width * 45 / 100 - 4)) {
            StringDrawer.drawString8(context, client, "▽", baseX + 1 + (int) (rollRate * width / 4), baseY + width * 45 / 100, color, false);
        }

        int hsx = baseX - (int) (hAngle * width / 900) + 1;
        if (edgeCheck(edge, 5, hsx, baseY + width * 45 / 100 - 1)) {
            StringDrawer.drawString8(context, client, "⭘", hsx, baseY + width * 45 / 100 + 7, color, false);
        }

        int x0 = baseX, y0 = baseY;
        x0 -= yawRate * width / 200;
        y0 -= pitchRate * width / 200;
        if (edgeCheck(edge, 5, x0, y0)) {
            context.renderOutline(x0 - 3, y0 - 2, 5, 5, color);
            OverlayRenderer.renderLine(context, x0 - 1, y0 - 5, x0 - 1, y0 - 2, color);
            OverlayRenderer.renderLine(context, x0 - 6, y0, x0 - 3, y0, color);
            OverlayRenderer.renderLine(context, x0 + 1, y0, x0 + 4, y0, color);
        }
    }

    @Override
    public void drawDials(GuiGraphics context, Minecraft client, int baseX, int baseY, int scale, EngineVehicle aircraft) {
        // dial 55x55
        context.fill(baseX - 27 * scale, baseY - 27 * scale, baseX + 27 * scale + 1, baseY + 27 * scale + 1, colorLt0);
        // cursor
        int xr = baseX - (int) (yawRate * 0.15d * scale);
        int yr = baseY - (int) (pitchRate * 0.15d * scale);
        int[] edge0 = new int[]{baseY - 27 * scale, baseY + 27 * scale, baseX - 27 * scale, baseX + 27 * scale};
        if (edgeCheck(edge0, 5, xr, yr)) {
            context.renderOutline(xr - 2, yr - 2, 5, 5, colorBG);
            OverlayRenderer.renderLine(context, xr, yr - 5, xr, yr - 2, colorBG);
            OverlayRenderer.renderLine(context, xr - 5, yr, xr - 2, yr, colorBG);
            OverlayRenderer.renderLine(context, xr + 2, yr, xr + 5, yr, colorBG);
        }
        StringDrawer.drawString5(context, client, "⏺", baseX - (int) (hAngle * scale / 6) + 2, baseY + 15 * scale + 1, colorHD1, true);

        // border
        context.fill(baseX - 27 * scale, baseY - 27 * scale, baseX + 27 * scale + 1, baseY - 19 * scale + 1, colorBG);
        context.fill(baseX - 27 * scale, baseY - 27 * scale, baseX - 19 * scale + 1, baseY + 27 * scale + 1, colorBG);
        context.fill(baseX + 19 * scale, baseY - 27 * scale, baseX + 27 * scale + 1, baseY + 27 * scale + 1, colorBG);
        context.fill(baseX - 27 * scale, baseY + 19 * scale, baseX + 27 * scale + 1, baseY + 27 * scale + 1, colorBG);

        OverlayRenderer.drawDialOutline(context, baseX, baseY, scale);

        context.fill(baseX - 18 * scale, baseY - 18 * scale, baseX + 18 * scale + 1, baseY - 17 * scale, colorSD);
        context.fill(baseX - 18 * scale, baseY - 18 * scale, baseX - 17 * scale, baseY + 18 * scale + 1, colorSD);
        context.fill(baseX - 19 * scale, baseY - 19 * scale, baseX + 19 * scale + 1, baseY - 18 * scale, colorFG);
        context.fill(baseX - 19 * scale, baseY - 19 * scale, baseX - 18 * scale, baseY + 19 * scale + 1, colorFG);

        context.fill(baseX - 19 * scale, baseY + 18 * scale + 1, baseX + 19 * scale + 1, baseY + 19 * scale + 1, colorFG);
        context.fill(baseX + 18 * scale + 1, baseY - 19 * scale, baseX + 19 * scale + 1, baseY + 19 * scale + 1, colorFG);

        // 0 marks
        context.fill(baseX - 25 * scale, baseY, baseX - 19 * scale + 1, baseY + 1, colorHD1);
        context.fill(baseX + 19 * scale, baseY, baseX + 25 * scale + 1, baseY + 1, colorHD1);
        context.fill(baseX, baseY - 25 * scale, baseX + 1, baseY - 19 * scale + 1, colorHD1);

        // glass tube
        OverlayRenderer.renderLine(context, baseX - 20 * scale, baseY + 14 * scale, baseX + 20 * scale, baseY + 14 * scale, colorG);
        OverlayRenderer.renderLine(context, baseX - 20 * scale, baseY + 18 * scale, baseX + 20 * scale, baseY + 18 * scale, colorG);
        OverlayRenderer.renderLine(context, baseX - 2 * scale, baseY + 14 * scale, baseX - 2 * scale, baseY + 18 * scale, colorHD1);
        OverlayRenderer.renderLine(context, baseX + 2 * scale, baseY + 14 * scale, baseX + 2 * scale, baseY + 18 * scale, colorHD1);
        OverlayRenderer.renderLine(context, baseX - 20 * scale, baseY + 14 * scale, baseX - 20 * scale, baseY + 18 * scale, colorFG);
        OverlayRenderer.renderLine(context, baseX + 20 * scale, baseY + 14 * scale, baseX + 20 * scale, baseY + 18 * scale, colorFG);

        // W
        OverlayRenderer.renderLine(context, baseX - 26 * scale, baseY - 18 * scale, baseX - 2 * scale, baseY - 18 * scale, colorHD2, false, true);
        OverlayRenderer.renderLine(context, baseX + 2 * scale, baseY - 18 * scale, baseX + 26 * scale, baseY - 18 * scale, colorHD2, false, true);
        StringDrawer.drawString2(context, client, "¯W¯", baseX + 2, baseY - 18 * scale - 1, colorHD2, true);
        if (scale > 1) {
            StringDrawer.drawString9(context, client, "TC", baseX + 20 * scale + 1, baseY + 25 * scale + 1, colorFG, false);
        } else {
            {
                // T
                int x = baseX + 13, y = baseY + 26;
                context.fill(x, y - 5, x + 3, y - 4, colorFG);
                context.fill(x + 1, y - 4, x + 2, y, colorFG);
            }
            {
                // C
                int x = baseX + 16, y = baseY + 26;
                context.fill(x + 1, y - 5, x + 3, y - 4, colorFG);
                context.fill(x, y - 4, x + 1, y - 1, colorFG);
                context.fill(x + 1, y - 1, x + 3, y, colorFG);
            }
        }
        for (int i = -16; i <= 16; i += 2) {
            context.fill(baseX + i * scale, baseY + 25 * scale + 1, baseX + i * scale + 1, baseY + 26 * scale + 1, Math.abs(i) >= 10 ? colorFG2 : colorBG);
        }

        // hands
        StringDrawer.drawString2(context, client, "⏷", baseX + (int) (rollRate * scale * 10) + 2, baseY + 25 * scale - 4, colorHD1, true);
    }
}
