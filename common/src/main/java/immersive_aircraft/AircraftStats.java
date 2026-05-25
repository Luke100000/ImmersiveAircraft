package immersive_aircraft;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class AircraftStats {
    public static ResourceLocation DISTANCE_TOTAL;
    public static ResourceLocation TIME_IN_AIRCRAFT;
    public static ResourceLocation FUEL_BURNED;
    public static ResourceLocation DAMAGE_RECEIVED;
    public static ResourceLocation CRASHES;

    public static void bootstrap() {
        DISTANCE_TOTAL = makeCustomStat("distance_total", StatFormatter.DISTANCE);
        TIME_IN_AIRCRAFT = makeCustomStat("time_in_aircraft", StatFormatter.TIME);
        FUEL_BURNED = makeCustomStat("fuel_burned", StatFormatter.DEFAULT);
        DAMAGE_RECEIVED = makeCustomStat("damage_received", StatFormatter.DEFAULT);
        CRASHES = makeCustomStat("crashes", StatFormatter.DEFAULT);
    }

    private static ResourceLocation makeCustomStat(String key, StatFormatter formatter) {
        ResourceLocation id = Main.locate(key);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, id, id);
        Stats.CUSTOM.get(id, formatter);
        return id;
    }
}
