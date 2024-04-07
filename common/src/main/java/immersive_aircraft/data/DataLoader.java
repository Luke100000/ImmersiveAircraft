package immersive_aircraft.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_aircraft.item.upgrade.VehicleUpgrade;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import org.jetbrains.annotations.NotNull;

public abstract class DataLoader extends SimpleJsonResourceReloadListener {
    public DataLoader(Gson gson, String string) {
        super(gson, string);
    }

    @NotNull
    static VehicleUpgrade getAircraftUpgrade(JsonObject jsonObject) {
        VehicleUpgrade upgrade = new VehicleUpgrade();
        for (String key : jsonObject.keySet()) {
            VehicleStat stat = VehicleStat.STATS.get(key);
            if (stat != null) {
                upgrade.set(stat, jsonObject.get(key).getAsFloat());
            }
        }
        return upgrade;
    }
}
