package immersive_aircraft;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public class AircraftStats {
    public static final ResourceLocation DISTANCE_TOTAL = makeCustomStat("distance_total", StatFormatter.DISTANCE);
    public static final ResourceLocation TIME_IN_AIRCRAFT = makeCustomStat("time_in_aircraft", StatFormatter.TIME);
    public static final ResourceLocation FUEL_BURNED = makeCustomStat("fuel_burned", StatFormatter.DEFAULT);
    public static final ResourceLocation DAMAGE_RECEIVED = makeCustomStat("damage_received", StatFormatter.DEFAULT);
    public static final ResourceLocation CRASHES = makeCustomStat("crashes", StatFormatter.DEFAULT);

    public static void bootstrap() {
    }

    private static ResourceLocation makeCustomStat(String key, StatFormatter formatter) {
        ResourceLocation id = Main.locate(key);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, id, id);
        Stats.CUSTOM.get(id, formatter);
        return id;
    }
}
