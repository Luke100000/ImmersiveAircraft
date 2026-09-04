package immersive_aircraft.combat;

import immersive_aircraft.client.gui.VehicleScreen;
import net.minecraft.client.renderer.Rect2i;

import java.util.stream.Stream;

public class CombatUtils {
    public static Stream<Rect2i> getAreas(VehicleScreen containerScreen) {
        final int x = containerScreen.getX();
        final int y = containerScreen.getY();

        Stream<Rect2i> inventoryAreas = containerScreen.getMenu().getVehicle().getInventoryDescription().getRectangles().stream()
                .map(r -> new Rect2i(
                        x + r.getX(),
                        y + r.getY(),
                        r.getWidth(),
                        r.getHeight()
                ));
        return Stream.concat(inventoryAreas, containerScreen.getSkinPanelArea().stream());
    }
}
