package immersive_aircraft.client.hud;

import immersive_aircraft.entity.EngineVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public interface Indicator {
    void update(Minecraft client, EngineVehicle aircraft);

    void drawDashboard(GuiGraphics context, Minecraft client, int baseX, int baseY, EngineVehicle aircraft, int color);

    void drawHUD(GuiGraphics context, Minecraft client, int baseX, int baseY, int width, EngineVehicle aircraft, int color, int[] edge);

    void drawDials(GuiGraphics context, Minecraft client, int baseX, int baseY, int scale, EngineVehicle aircraft);

    default boolean edgeCheck(int[] edge, int r, int x, int y) {
        return edgeCheck(edge, r, r, x, y);
    }

    default boolean edgeCheck(int[] edge, int rx, int ry, int x, int y) {
        return edge == null || y - ry > edge[0]
                               && y + ry < edge[1]
                               && x - rx > edge[2]
                               && x + rx < edge[3];
    }
}
