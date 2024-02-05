package immersive_aircraft.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.item.upgrade.AircraftUpgrade;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import org.jetbrains.annotations.NotNull;

public abstract class DataLoader extends SimpleJsonResourceReloadListener {
    public DataLoader(Gson gson, String string) {
        super(gson, string);
    }

    @NotNull
    static AircraftUpgrade getAircraftUpgrade(JsonObject jsonObject) {
        AircraftUpgrade upgrade = new AircraftUpgrade();
        for (String key : jsonObject.keySet()) {
            AircraftStat stat = AircraftStat.STATS.get(key);
            if (stat != null) {
                upgrade.set(stat, jsonObject.get(key).getAsFloat());
            }
        }
        return upgrade;
    }
}
