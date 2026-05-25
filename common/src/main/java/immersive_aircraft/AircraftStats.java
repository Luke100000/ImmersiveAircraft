package immersive_aircraft;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class AircraftStats {
    public static Identifier DISTANCE_TOTAL;
    public static Identifier TIME_IN_AIRCRAFT;
    public static Identifier FUEL_BURNED;
    public static Identifier DAMAGE_RECEIVED;
    public static Identifier CRASHES;

    public static void bootstrap() {
        DISTANCE_TOTAL = makeCustomStat("distance_total", StatFormatter.DISTANCE);
        TIME_IN_AIRCRAFT = makeCustomStat("time_in_aircraft", StatFormatter.TIME);
        FUEL_BURNED = makeCustomStat("fuel_burned", StatFormatter.DEFAULT);
        DAMAGE_RECEIVED = makeCustomStat("damage_received", StatFormatter.DEFAULT);
        CRASHES = makeCustomStat("crashes", StatFormatter.DEFAULT);
    }

    private static Identifier makeCustomStat(String key, StatFormatter formatter) {
        Identifier id = Main.locate(key);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, id, id);
        Stats.CUSTOM.get(id, formatter);
        return id;
    }
}
