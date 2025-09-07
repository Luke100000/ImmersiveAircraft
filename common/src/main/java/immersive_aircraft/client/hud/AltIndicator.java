package immersive_aircraft.client.hud;

import immersive_aircraft.client.OverlayRenderer;
import immersive_aircraft.entity.EngineVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.stream.IntStream;

import static immersive_aircraft.client.hud.Colors.*;

public class AltIndicator implements Indicator {
    public static final AltIndicator INSTANCE = new AltIndicator();

    private double lastAlt = 0;
    private static final int widthHalf = 100;
    private static final int bHeightHalf = 35;

    double vSpeed = 0;
    double iSpeed = 0;
    double iSpeedRt = 0;

    @Override
    public void update(Minecraft client, EngineVehicle aircraft) {
        // altitude is relative to sea level (y=63 in OverWorld)
        lastAlt = aircraft.getY() - aircraft.level().getSeaLevel();
        double speed = aircraft.getSpeedVector().y * 10.0d;
        if (speed != vSpeed) {
            iSpeed = speed - vSpeed;
            iSpeedRt = -iSpeed / 30;
            vSpeed = speed;
        }
        if ((iSpeed + iSpeedRt) * iSpeed > 0) {
            iSpeed += iSpeedRt;
        } else {
            iSpeed = 0;
            iSpeedRt = 0;
        }
    }

    public void drawDashboard(GuiGraphics context, Minecraft client, int baseX, int baseY, EngineVehicle aircraft, int color) {
        char rt = '-';
        if (vSpeed < -10) {
            rt = '▼';
        } else if (vSpeed < -1) {
            rt = '⏷';
        } else if (vSpeed > 10) {
            rt = '▲';
        } else if (vSpeed > 1) {
            rt = '⏶';
        }
        StringDrawer.drawString3(context, client, String.format("ALT %3.1f%s", lastAlt, rt), baseX + widthHalf + 5, baseY, color, true);
    }

    @Override
    public void drawHUD(GuiGraphics context, Minecraft client, int baseX, int baseY, int width, EngineVehicle aircraft, int color, int[] edge) {
        for (int i = -bHeightHalf; i < bHeightHalf; i += 5) {
            if (edgeCheck(edge, 2, baseX, baseY + i * width / 100)) {
                OverlayRenderer.renderLine(context, baseX, baseY + i * width / 100 - 1, baseX, baseY + (i + 5) * width / 100 + 1, color);
            }
        }

        IntStream.range(-14, 16).forEach(value -> {
            int x = baseX + 4 - (Math.abs(value) >= 10 ? 1 : 0), y = baseY + value * width / 40;
            if (edgeCheck(edge, 2, x, y)) {
                StringDrawer.drawString6(context, client, "‑", x, y, color, false);
            }
        });

        if (edgeCheck(edge, 5, baseX, baseY)) {
            StringDrawer.drawString4(context, client, "  ALT", baseX + 10, baseY - client.font.lineHeight, color, false);
            StringDrawer.drawString4(context, client, String.format("-{%3.1f]", lastAlt), baseX, baseY, color, false);
        }

        int iz = (int) lastAlt;
        int nearest1_10 = Math.floorDiv(iz, 10);
        Iterator<Integer> it = IntStream.range(nearest1_10 - 5, nearest1_10 + 6).iterator();
        while (it.hasNext()) {
            int v = it.next();
            String vp = v % 10 == 0 ? v == nearest1_10 ? "─" : '·' + String.valueOf(v / 10) + '·' : v % 5 == 0 ? "─" : "-";
            int yy = baseY + (iz - v * 10) * width / 100;
            if (edgeCheck(edge, 5, baseX, yy)) {
                StringDrawer.drawString4(context, client, vp, baseX, yy, color, false);
            }
        }

        int yr = baseY - (int) ((vSpeed - iSpeed) * width / 40);
        if (edgeCheck(edge, 3, baseX + 1, yr)) {
            StringDrawer.drawString6(context, client, "▷", baseX + 1, yr, color, false);
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
        Vec3 scale4 = new Vec3(0, 10 * scale, 0);
        for (int i = 0; i < 100; i += 2) {
            float angle = -(float) Math.toRadians(3.6f * i);
            Vec3 sa = scale0.zRot(angle);
            Vec3 sb = i % 10 == 0 ? scale2.zRot(angle) : scale1.zRot(angle);
            OverlayRenderer.renderLine(context, baseX + (int) sa.x, baseY + (int) sa.y, baseX + (int) sb.x, baseY + (int) sb.y, colorFG);
            if (i % 10 == 0) {
                Vec3 sc = scale3.zRot(angle), sd = scale4.zRot(angle);
                StringDrawer.drawString5(context, client, String.valueOf(i / 10), baseX + (int) sc.x + 2, baseY + (int) sc.y - 1, colorFG, false);
                if (i % 50 == 0) {
                    StringDrawer.drawString5(context, client, StringDrawer.toSubScript(String.valueOf(i / 5)), baseX + (int) sd.x + 2, baseY + (int) sd.y - 1, colorFG, false);
                } else {
                    StringDrawer.drawString5(context, client, StringDrawer.toSubScript(String.valueOf((i / 5) % 10)), baseX + (int) sd.x + 2, baseY + (int) sd.y - 1, colorFG, false);
                }
            }
        }

        // hands
        float angle = -(float) Math.toRadians(lastAlt * 3.6f);
        float angle2 = -(float) Math.toRadians(Math.max(0, Math.min(2000, lastAlt)) * 0.18f);
        Vec3 h1 = scale1.zRot(angle), h2 = scale3.zRot(angle2);
        OverlayRenderer.renderLine(context, baseX, baseY, baseX + (int) h1.x, baseY + (int) h1.y, colorHD1, false, true);
        OverlayRenderer.renderLine(context, baseX, baseY, baseX + (int) h2.x, baseY + (int) h2.y, colorHD2, false, true);

        OverlayRenderer.drawDialOutline(context, baseX, baseY, scale);

        if (scale > 1) {
            StringDrawer.drawString9(context, client, "ALT", baseX + 20 * scale + 1, baseY + 25 * scale + 1, colorFG, false);
        } else {
            {
                // A
                int x = baseX + 11, y = baseY + 26;
                context.fill(x, y - 4, x + 1, y, colorFG);
                context.fill(x + 1, y - 5, x + 2, y - 4, colorFG);
                context.fill(x + 2, y - 4, x + 3, y, colorFG);
                context.fill(x + 1, y - 3, x + 2, y - 2, colorFG);
            }
            {
                // L
                int x = baseX + 14, y = baseY + 26;
                context.fill(x + 1, y - 5, x + 2, y - 1, colorFG);
                context.fill(x + 1, y - 1, x + 3, y, colorFG);
            }
            {
                // T
                int x = baseX + 17, y = baseY + 26;
                context.fill(x, y - 5, x + 3, y - 4, colorFG);
                context.fill(x + 1, y - 4, x + 2, y, colorFG);
            }
        }

        for (int i = -20; i <= 20; i += 2) {
            context.fill(baseX - 26 * scale, baseY + i * scale, baseX - 25 * scale, baseY + i * scale + 1, Math.abs(i) > 10 ? colorFG2 : colorBG);
        }

        // hands
        StringDrawer.drawString6(context, client, "⏴", baseX - 25 * scale + 4, baseY - (int) (Math.max(Math.min(vSpeed - iSpeed, 20), -20) * scale), colorHD1, true);

        // caution lamp
        context.fill(baseX - 3 * scale, baseY - 3 * scale, baseX + 3 * scale + 1, baseY + 3 * scale + 1, colorFG);
        context.fill(baseX - 2 * scale, baseY - 2 * scale, baseX + 2 * scale + 1, baseY + 2 * scale + 1,
                lastAlt < 0 || lastAlt >= 2000 || WarningIndicator.INSTANCE.cMap.get(EngineVehicle.Cautions.PULL_UP) ? colorLt1 : colorLt0);
    }
}
