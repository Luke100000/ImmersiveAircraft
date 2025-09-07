package immersive_aircraft.client.hud;

import immersive_aircraft.client.OverlayRenderer;
import immersive_aircraft.entity.EngineVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.stream.IntStream;

import static immersive_aircraft.client.hud.Colors.*;

public class SpeedIndicator implements Indicator {
    public static final SpeedIndicator INSTANCE = new SpeedIndicator();

    private Vec3 lastSpeed = new Vec3(0, 0, 0);
    private Vec3 iSpeed = new Vec3(0, 0, 0);
    private Vec3 iSpeedRt = new Vec3(0, 0, 0);
    private Vec3 mSpeed = new Vec3(0, 0, 0);
    private Vec3 dir = new Vec3(0, 0, 0);

    private static final int widthHalf = 100;
    private static final int bHeightHalf = 35;

    @Override
    public void update(Minecraft client, EngineVehicle aircraft) {
        Vec3 speed = aircraft.getSpeedVector().scale(20.0d);
        dir = Vec3.directionFromRotation(aircraft.getRotationVector());
        if (!speed.equals(lastSpeed)) {
            iSpeed = speed.add(lastSpeed.reverse());
            iSpeedRt = iSpeed.scale(-1 / 30.0f);
            lastSpeed = speed;
        }
        if (iSpeed.add(iSpeedRt).dot(iSpeed) > 0) iSpeed = iSpeed.add(iSpeedRt);
        else {
            iSpeed = new Vec3(0, 0, 0);
            iSpeedRt = new Vec3(0, 0, 0);
        }
        mSpeed = lastSpeed.add(iSpeed.reverse());
    }

    public void drawDashboard(GuiGraphics context, Minecraft client, int baseX, int baseY, EngineVehicle aircraft, int color) {
        StringDrawer.drawString1(context, client, String.format("AS %2.1f", lastSpeed.dot(dir)), baseX - widthHalf, baseY, color, true);
    }

    @Override
    public void drawHUD(GuiGraphics context, Minecraft client, int baseX, int baseY, int width, EngineVehicle aircraft, int color, int[] edge) {
        for (int i = -bHeightHalf; i < bHeightHalf; i += 5)
            if (edgeCheck(edge, 2, baseX - 3, baseY + i * width / 100))
                OverlayRenderer.renderLine(context, baseX - 3, baseY + i * width / 100 - 1, baseX - 3, baseY + (i + 5) * width / 100 + 1, color);
        if (edgeCheck(edge, 5, baseX, baseY)) {
            StringDrawer.drawString6(context, client, "AS", baseX - 10, baseY - client.font.lineHeight, color, false);
            StringDrawer.drawString6(context, client, String.format("[%2.1f}-", lastSpeed.dot(dir)), baseX, baseY, color, false);
        }
        int iz = Double.valueOf(mSpeed.dot(dir) * 10).intValue();
        int nearest1_10 = Math.floorDiv(iz, 10);
        Iterator<Integer> it = IntStream.range(nearest1_10 - 5, nearest1_10 + 6).iterator();
        while (it.hasNext()) {
            int v = it.next();
            String vp = v % 10 == 0 ? v == nearest1_10 ? "─" : '·' + String.valueOf(v / 10) + '·' : v % 5 == 0 ? "─" : "-";
            int yy = baseY + (iz - v * 10) * width / 100;
            if (edgeCheck(edge, 5, baseX, yy))
                StringDrawer.drawString6(context, client, vp, baseX, yy, color, false);
        }
    }

    @Override
    public void drawDials(GuiGraphics context, Minecraft client, int baseX, int baseY, int scale, EngineVehicle aircraft) {
        // dial 55x55
        context.fill(baseX - 27 * scale, baseY - 27 * scale, baseX + 27 * scale + 1, baseY + 27 * scale + 1, colorBG);

        Vec3 scale0 = new Vec3(0, 25 * scale, 0);
        Vec3 scale1 = new Vec3(0, 22 * scale, 0);
        Vec3 scale2 = new Vec3(0, 20 * scale, 0);
        Vec3 scale3 = new Vec3(0, 16 * scale, 0);
        for (int i = 0; i < 60; i += 1) {
            float angle = -(float) Math.toRadians(6.0f * i);
            Vec3 sa = scale0.zRot(angle);
            Vec3 sb = i % 10 == 0 ? scale2.zRot(angle) : scale1.zRot(angle);
            OverlayRenderer.renderLine(context, baseX + (int) sa.x, baseY + (int) sa.y, baseX + (int) sb.x, baseY + (int) sb.y, colorFG);
            if (i % 10 == 0) {
                Vec3 sc = scale3.zRot(angle);
                StringDrawer.drawString5(context, client, String.valueOf(i / 10), baseX + (int) sc.x + 2, baseY + (int) sc.y - 1, colorFG, false);
            }
        }
        int iz = Double.valueOf(mSpeed.dot(dir) * 10).intValue();

        // hands
        float angle = -(float) Math.toRadians(iz * 0.6f);
        Vec3 h1 = scale1.zRot(angle);
        OverlayRenderer.renderLine(context, baseX, baseY, baseX + (int) h1.x, baseY + (int) h1.y, colorHD1, false, true);

        OverlayRenderer.drawDialOutline(context, baseX, baseY, scale);

        if (scale > 1)
            StringDrawer.drawString9(context, client, "AS", baseX + 20 * scale + 1, baseY + 25 * scale + 1, colorFG, false);
        else {
            {
                // A
                int x = baseX + 13, y = baseY + 26;
                context.fill(x, y - 4, x + 1, y, colorFG);
                context.fill(x + 1, y - 5, x + 2, y - 4, colorFG);
                context.fill(x + 2, y - 4, x + 3, y, colorFG);
                context.fill(x + 1, y - 3, x + 2, y - 2, colorFG);
            }
            {
                // S
                int x = baseX + 16, y = baseY + 26;
                context.fill(x + 1, y - 5, x + 3, y - 4, colorFG);
                context.fill(x, y - 4, x + 1, y - 3, colorFG);
                context.fill(x + 1, y - 3, x + 2, y - 2, colorFG);
                context.fill(x + 2, y - 3, x + 3, y - 1, colorFG);
                context.fill(x, y - 1, x + 2, y, colorFG);
            }
        }

        // out of range lamp
        context.fill(baseX - 3 * scale, baseY - 3 * scale, baseX + 3 * scale + 1, baseY + 3 * scale + 1, colorFG);
        context.fill(baseX - 2 * scale, baseY - 2 * scale, baseX + 2 * scale + 1, baseY + 2 * scale + 1, iz >= 0 && iz < 600 ? colorLt0 : colorLt1);
    }
}
