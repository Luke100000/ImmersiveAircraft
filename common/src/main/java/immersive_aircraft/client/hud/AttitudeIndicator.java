package immersive_aircraft.client.hud;

import immersive_aircraft.client.OverlayRenderer;
import immersive_aircraft.entity.EngineVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;

import java.util.stream.IntStream;

import static immersive_aircraft.client.hud.Colors.*;

public class AttitudeIndicator implements Indicator {
    public static final AttitudeIndicator INSTANCE = new AttitudeIndicator();
    private float yawRate = 0;
    private float pitch = 0;
    private float roll = 0;

    @Override
    public void update(Minecraft client, EngineVehicle aircraft) {
        pitch = aircraft.getXRot();
        float yaw = aircraft.getYRot();
        yawRate = (yaw - aircraft.yRotO) * 20;
        roll = aircraft.getRoll();
    }

    public void drawDashboard(GuiGraphics context, Minecraft client, int baseX, int baseY, EngineVehicle aircraft, int color) {
    }

    @Override
    public void drawHUD(GuiGraphics context, Minecraft client, int baseX, int baseY, int width, EngineVehicle aircraft, int color, int[] edge) {
        // Pitch ladder
        Vec3 vecBase = new Vec3(baseX - 1, baseY, 0);
        float roll2 = (float) Math.toRadians(roll);
        IntStream.of(-90, -75, -60, -45, -30, -15, 0, 15, 30, 45, 60, 75, 90).forEach(value -> {
            int y = -(int) (Math.sin(Math.toRadians(value)) * 100);
            y -= (int) (Math.sin(Math.toRadians(pitch)) * 100);
            if (Math.abs(y) > 55) return;
            int xn = (int) (yawRate / 20);
            int x1 = value == 0 ? -40 : -32;
            int x2 = switch (value) {
                case -75, -45, -15, 15, 45, 75 -> -23;
                case -90, -60, -30, 0, 30, 60, 90 -> -12;
                default -> -12;
            };
            int x3 = -x2;
            int x4 = -x1;
            Vec3 v1 = new Vec3(x1 - xn, y, 0).zRot(roll2).scale(width / 100.0f).add(vecBase),
                    v2 = new Vec3(x2 - xn, y, 0).zRot(roll2).scale(width / 100.0f).add(vecBase),
                    v3 = new Vec3(x3 - xn, y, 0).zRot(roll2).scale(width / 100.0f).add(vecBase),
                    v4 = new Vec3(x4 - xn, y, 0).zRot(roll2).scale(width / 100.0f).add(vecBase),
                    v5 = new Vec3(x1 - xn, y + 3, 0).zRot(roll2).scale(width / 100.0f).add(vecBase),
                    v6 = new Vec3(x4 - xn, y + 3, 0).zRot(roll2).scale(width / 100.0f).add(vecBase);
            int[][] lgroup = new int[][]{new int[]{(int) v1.x, (int) v1.y, (int) v2.x, (int) v2.y}, new int[]{(int) v3.x, (int) v3.y, (int) v4.x, (int) v4.y}};
            int[][] lgroup2 = new int[][]{new int[]{(int) v1.x, (int) v1.y, (int) v5.x, (int) v5.y}, new int[]{(int) v4.x, (int) v4.y, (int) v6.x, (int) v6.y}};
            for (int[] l : lgroup) {
                if (edgeCheck(edge, Math.abs(l[2] - l[0]) / 2, 2, (l[0] + l[2]) / 2, (l[1] + l[3]) / 2)) {
                    OverlayRenderer.renderLine(context, l[0], l[1], l[2], l[3], color, value < 0);
                }
            }
            if (value != 0) {
                for (int[] l : lgroup2) {
                    if (edgeCheck(edge, 2, l[0], l[1])) {
                        OverlayRenderer.renderLine(context, l[0], l[1], l[2], l[3], color);
                    }
                }
                switch (Math.abs(value)) {
                    case 15, 45, 75 -> {
                        if (edgeCheck(edge, 2, (int) v2.x, (int) v2.y)) {
                            StringDrawer.drawString1(context, client, StringDrawer.toSuperScript(String.valueOf(Math.abs(value))), (int) v2.x, (int) v2.y, color, false);
                        }
                        if (edgeCheck(edge, 2, (int) v3.x, (int) v3.y)) {
                            StringDrawer.drawString3(context, client, StringDrawer.toSuperScript(String.valueOf(Math.abs(value))), (int) v3.x, (int) v3.y, color, false);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void drawDials(GuiGraphics context, Minecraft client, int baseX, int baseY, int scale, EngineVehicle aircraft) {
        // dial 55x55
        context.fill(baseX - 27 * scale, baseY - 27 * scale, baseX + 27 * scale + 1, baseY + 27 * scale + 1, colorLt0);

        // Pitch ladder
        Vec3 vecBase = new Vec3(baseX, baseY, 0);
        float roll2 = (float) Math.toRadians(roll);
        Vec3 v0 = new Vec3(0, 4, 0).scale(scale);
        Vec3 vd = new Vec3((int) (yawRate / 20), -(int) (Math.sin(Math.toRadians(pitch)) * 40), 0).scale(scale);
        int[] edge0 = new int[]{baseY - 27 * scale, baseY + 27 * scale, baseX - 27 * scale, baseX + 27 * scale};
        for (int i = 0; i < 10; i++) {
            Vec3 v01 = v0.scale(i);
            Vec3 v02 = v0.scale(i + 1).add(0, scale, 0);
            Vec3 v1 = v01.add(vd).zRot(roll2).add(vecBase);
            Vec3 v2 = v02.add(vd).zRot(roll2).add(vecBase);
            if (edgeCheck(edge0, 2 * scale, (int) (v1.x + v2.x) / 2, (int) (v1.y + v2.y) / 2)) {
                OverlayRenderer.renderLine(context, (int) v2.x, (int) v2.y, (int) v1.x, (int) v1.y, colorL2);
            }
            v1 = v01.reverse().add(vd).zRot(roll2).add(vecBase);
            v2 = v02.reverse().add(vd).zRot(roll2).add(vecBase);
            if (edgeCheck(edge0, 2 * scale, (int) (v1.x + v2.x) / 2, (int) (v1.y + v2.y) / 2)) {
                OverlayRenderer.renderLine(context, (int) v2.x, (int) v2.y, (int) v1.x, (int) v1.y, colorL1);
            }
        }
        IntStream.of(-90, -75, -60, -45, -30, -15, 0, 15, 30, 45, 60, 75, 90).forEach(value -> {
            int y = -(int) (Math.sin(Math.toRadians(value)) * 40);
            int x1 = 20;
            int x2 = 10;
            int x3 = -x2;
            int x4 = -x1;
            Vec3 v1 = new Vec3(x1, y, 0).scale(scale).add(vd).zRot(roll2).add(vecBase),
                    v2 = new Vec3(x2, y, 0).scale(scale).add(vd).zRot(roll2).add(vecBase),
                    v3 = new Vec3(x3, y, 0).scale(scale).add(vd).zRot(roll2).add(vecBase),
                    v4 = new Vec3(x4, y, 0).scale(scale).add(vd).zRot(roll2).add(vecBase),
                    v5 = new Vec3(0, y, 0).scale(scale).add(vd).zRot(roll2).add(vecBase);
            if (value != 0) {
                if (edgeCheck(edge0, 2 * scale, (int) (v5.x + v2.x) / 2, (int) (v5.y + v2.y) / 2)) {
                    OverlayRenderer.renderLine(context, (int) v2.x, (int) v2.y, (int) v5.x, (int) v5.y, value > 0 ? colorL1 : colorL2);
                }
                if (edgeCheck(edge0, 2 * scale, (int) (v5.x + v3.x) / 2, (int) (v5.y + v3.y) / 2)) {
                    OverlayRenderer.renderLine(context, (int) v3.x, (int) v3.y, (int) v5.x, (int) v5.y, value > 0 ? colorL1 : colorL2);
                }
                if (value % 30 == 0) {
                    if (edgeCheck(edge0, 2 * scale, (int) (v1.x + v2.x) / 2, (int) (v1.y + v2.y) / 2)) {
                        OverlayRenderer.renderLine(context, (int) v2.x, (int) v2.y, (int) v1.x, (int) v1.y, value > 0 ? colorL1 : colorL2);
                    }
                    if (edgeCheck(edge0, 2 * scale, (int) (v4.x + v3.x) / 2, (int) (v4.y + v3.y) / 2)) {
                        OverlayRenderer.renderLine(context, (int) v3.x, (int) v3.y, (int) v4.x, (int) v4.y, value > 0 ? colorL1 : colorL2);
                    }
                }
            } else {
                Vec3 v00 = new Vec3(4, 0, 0).scale(scale);
                int i = 0;
                while (true) {
                    Vec3 v01 = v00.scale(i).add(vd).zRot(roll2).add(vecBase);
                    Vec3 v02 = v00.scale(i + 1).add(scale, 0, 0).add(vd).zRot(roll2).add(vecBase);
                    if (edgeCheck(edge0, 2 * scale, (int) (v01.x + v02.x) / 2, (int) (v01.y + v02.y) / 2)) {
                        OverlayRenderer.renderLine(context, (int) v02.x, (int) v02.y, (int) v01.x, (int) v01.y, colorL3);
                    } else {
                        break;
                    }
                    i++;
                }
                i = 0;
                while (true) {
                    Vec3 v01 = v00.scale(i).add(vd).zRot(roll2).add(vecBase);
                    Vec3 v02 = v00.scale(i - 1).add(-scale, 0, 0).add(vd).zRot(roll2).add(vecBase);
                    if (edgeCheck(edge0, 3 * scale, (int) (v01.x + v02.x) / 2, (int) (v01.y + v02.y) / 2)) {
                        OverlayRenderer.renderLine(context, (int) v02.x, (int) v02.y, (int) v01.x, (int) v01.y, colorL3);
                    } else {
                        break;
                    }
                    i--;
                }
            }
        });

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
        context.fill(baseX - 25 * scale, baseY, baseX - 19 * scale + 1, baseY + 1, colorHD1);
        context.fill(baseX + 19 * scale, baseY, baseX + 25 * scale + 1, baseY + 1, colorHD1);
        context.fill(baseX, baseY - 25 * scale, baseX + 1, baseY - 19 * scale + 1, colorHD1);
        context.fill(baseX, baseY + 19 * scale, baseX + 1, baseY + 25 * scale + 1, colorHD1);
        if (scale > 1) {
            StringDrawer.drawString9(context, client, "AI", baseX + 20 * scale + 1, baseY + 25 * scale + 1, colorFG, false);
        } else {
            {
                // A
                int x = baseX + 13, y = baseY + 26;
                context.fill(x, y - 4, x + 1, y, colorFG);
                context.fill(x + 1, y - 5, x + 2, y - 4, colorFG);
                context.fill(x + 2, y - 4, x + 3, y, colorFG);
                context.fill(x + 1, y - 3, x + 2, y - 2, colorFG);
            }
            {
                // I
                int x = baseX + 16, y = baseY + 26;
                context.fill(x + 1, y - 5, x + 2, y, colorFG);
            }
        }
    }
}
