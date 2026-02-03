package immersive_aircraft.combat;

import immersive_aircraft.client.gui.VehicleScreen;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;

public class REICombat implements REIClientPlugin {
    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(VehicleScreen.class, screen -> CombatUtils.getAreas(screen).map(v -> new Rectangle(v.getX(), v.getY(), v.getWidth(), v.getHeight())).toList());
    }
}
