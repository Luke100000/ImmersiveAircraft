package immersive_aircraft.client.hud;

import immersive_aircraft.client.OverlayRenderer;
import immersive_aircraft.entity.EngineVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Iterator;
import java.util.stream.IntStream;

import static immersive_aircraft.client.hud.Colors.*;

public class AzimuthIndicator implements Indicator {
    public static final AzimuthIndicator INSTANCE = new AzimuthIndicator();
    private double lastAz = 0;
    private boolean miniHUD = false;

    @Override
    public void update(Minecraft client, EngineVehicle aircraft) {
        lastAz = aircraft.getYRot();
    }

    public void drawDashboard(GuiGraphics context, Minecraft client, int baseX, int baseY, EngineVehicle aircraft, int color) {
        miniHUD = true;
        drawHUD(context, client, baseX, baseY + 9, 100, aircraft, color, null);
        miniHUD = false;
    }

    public void drawHUD(GuiGraphics context, Minecraft client, int baseX, int baseY, int width, EngineVehicle aircraft, int color, int[] edge) {
        double az = lastAz;
        while (az < 0) {
            az += 360;
        }
        while (az >= 360) {
            az -= 360;
        }
        if (miniHUD) {
            StringDrawer.drawString8(context, client, "▽", baseX + 1, baseY - 6, color, miniHUD);
        } else if (edgeCheck(edge, 5, baseX, baseY + 2)) {
            StringDrawer.drawString2(context, client, "△", baseX + 1, baseY - 1, color, miniHUD);
        }
        int iz = (int) az;
        int nearest1_5 = Math.floorDiv(iz, 5);
        Iterator<Integer> it = IntStream.range(nearest1_5 - 9, nearest1_5 + 10).iterator();
        while (it.hasNext()) {
            int v = it.next();
            int vl = v < 0 ? v + 72 : v;
            String vp = switch (vl) {
                case 0, 72 -> "S";
                case 9 -> "SW";
                case 18 -> "W";
                case 27 -> "NW";
                case 36 -> "N";
                case 45 -> "NE";
                case 54 -> "E";
                case 63 -> "SE";
                default -> (vl & 1) == 0 ? "|" : "ᛧ";
            };
            if (width < 200) {
                vp = switch (vl) {
                    case 8, 10 -> nearest1_5 > 0 && nearest1_5 <= 18 ? "" : vp;
                    case 26, 28 -> nearest1_5 > 18 && nearest1_5 <= 36 ? "" : vp;
                    case 44, 46 -> nearest1_5 > 36 && nearest1_5 <= 54 ? "" : vp;
                    case 62, 64 -> nearest1_5 > 54 && nearest1_5 <= 72 ? "" : vp;
                    default -> vp;
                };
            }
            if (width < 100) {
                vp = switch (vl) {
                    case 17, 19 -> nearest1_5 > 9 && nearest1_5 <= 27 ? "" : vp;
                    case 35, 37 -> nearest1_5 > 27 && nearest1_5 <= 45 ? "" : vp;
                    case 53, 55 -> nearest1_5 > 45 && nearest1_5 <= 63 ? "" : vp;
                    case 1, 71 -> nearest1_5 > 63 || nearest1_5 <= 9 ? "" : vp;
                    default -> vp;
                };
            }
            int xx = baseX - (iz - v * 5) * width / 100;
            if (edgeCheck(edge, 5, xx, baseY)) {
                StringDrawer.drawString8(context, client, vp, xx, baseY, color, miniHUD);
            }
        }
    }

    @Override
    public void drawDials(GuiGraphics context, Minecraft client, int baseX, int baseY, int scale, EngineVehicle aircraft) {
        double az = lastAz;
        while (az < 0) {
            az += 360;
        }
        while (az >= 360) {
            az -= 360;
        }
        // dial 109x19, scale max to 2
        scale = Math.min(scale, 2);
        context.fill(baseX - 54 * scale, baseY - 9 * scale, baseX + 54 * scale + 1, baseY + 9 + 1, colorBG);

        // compass
        int iz = (int) az;
        int[] edge = new int[]{baseY - 7, baseY + 7, baseX - 52 * scale, baseX + 52 * scale};
        int nearest1_5 = Math.floorDiv(iz, 5);
        Iterator<Integer> it = IntStream.range(nearest1_5 - 9, nearest1_5 + 10).iterator();
        while (it.hasNext()) {
            int v = it.next();
            int vl = v < 0 ? v + 72 : v;
            String vp = switch (vl) {
                case 0, 72 -> "S";
                case 9 -> "SW";
                case 18 -> "W";
                case 27 -> "NW";
                case 36 -> "N";
                case 45 -> "NE";
                case 54 -> "E";
                case 63 -> "SE";
                default -> (vl & 1) == 0 ? "|" : "ᛧ";
            };
            if (scale < 2) {
                vp = switch (vl) {
                    case 8, 10 -> nearest1_5 > 0 && nearest1_5 <= 18 ? "" : vp;
                    case 26, 28 -> nearest1_5 > 18 && nearest1_5 <= 36 ? "" : vp;
                    case 44, 46 -> nearest1_5 > 36 && nearest1_5 <= 54 ? "" : vp;
                    case 62, 64 -> nearest1_5 > 54 && nearest1_5 <= 72 ? "" : vp;
                    default -> vp;
                };
            }
            int xx = baseX - (iz - v * 5) * scale;
            if (edgeCheck(edge, 5, xx, baseY)) {
                StringDrawer.drawString5(context, client, vp, xx, baseY, colorFG, miniHUD);
            }
        }

        // border
        context.fill(baseX - 54 * scale, baseY - 9 * scale, baseX + 54 * scale + 1, baseY - 5 * scale + 1, colorBG);
        context.fill(baseX - 54 * scale, baseY - 9 * scale, baseX - 42 * scale + 1, baseY + 9 * scale + 1, colorBG);
        context.fill(baseX + 42 * scale, baseY - 9 * scale, baseX + 54 * scale + 1, baseY + 9 * scale + 1, colorBG);
        context.fill(baseX - 54 * scale, baseY + 5 * scale, baseX + 54 * scale + 1, baseY + 9 * scale + 1, colorBG);
        context.fill(baseX - 54 * scale, baseY - 9 * scale, baseX + 54 * scale + 1, baseY - 7 * scale, colorFG);
        context.fill(baseX - 54 * scale, baseY - 9 * scale, baseX - 52 * scale, baseY + 9 * scale + 1, colorFG);
        context.fill(baseX - 54 * scale, baseY + 7 * scale + 1, baseX + 54 * scale + 1, baseY + 9 * scale + 1, colorFG);
        context.fill(baseX + 52 * scale + 1, baseY - 9 * scale, baseX + 54 * scale + 1, baseY + 9 * scale + 1, colorFG);
        OverlayRenderer.drawScrew(context, baseX - 49 * scale, baseY, scale, true, colorFG);
        OverlayRenderer.drawScrew(context, baseX + 49 * scale, baseY, scale, false, colorFG);
        context.fill(baseX - 41 * scale, baseY - 4 * scale, baseX + 41 * scale + 1, baseY - 3 * scale, colorSD);
        context.fill(baseX - 41 * scale, baseY - 4 * scale, baseX - 40 * scale, baseY + 4 * scale + 1, colorSD);
        context.fill(baseX - 42 * scale, baseY - 5 * scale, baseX + 42 * scale + 1, baseY - 4 * scale, colorFG);
        context.fill(baseX - 42 * scale, baseY - 5 * scale, baseX - 41 * scale, baseY + 5 * scale + 1, colorFG);
        context.fill(baseX - 42 * scale, baseY + 4 * scale + 1, baseX + 42 * scale + 1, baseY + 5 * scale + 1, colorFG);
        context.fill(baseX + 41 * scale + 1, baseY - 5 * scale, baseX + 42 * scale + 1, baseY + 5 * scale + 1, colorFG);
        OverlayRenderer.renderLine(context, baseX, baseY - 5 * scale, baseX, baseY + 5 * scale, colorHD1, false, true);
        StringDrawer.drawString8(context, client, "⏷", baseX + 2, baseY - 6 * scale + 7, colorHD1, false);
        StringDrawer.drawString2(context, client, "⏶", baseX + 2, baseY + 6 * scale - 6, colorHD1, false);
        if (scale > 1) {
            StringDrawer.drawString9(context, client, "AI", baseX + 50 * scale + 1, baseY + 7 * scale + 1, colorFG, false);
        } else {
            {
                // H
                int x = baseX + 43, y = baseY + 8;
                context.fill(x, y - 5, x + 1, y, colorFG);
                context.fill(x + 2, y - 5, x + 3, y, colorFG);
                context.fill(x + 1, y - 3, x + 2, y - 2, colorFG);
            }
            {
                // I
                int x = baseX + 46, y = baseY + 8;
                context.fill(x + 1, y - 5, x + 2, y, colorFG);
            }
        }
    }
}
