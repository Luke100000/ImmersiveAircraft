package immersive_aircraft.config;

import immersive_aircraft.config.configEntries.BooleanConfigEntry;
import immersive_aircraft.config.configEntries.FloatConfigEntry;

import java.util.Map;

public final class Config extends JsonConfig {
    private static final Config INSTANCE = loadOrCreate();

    public static Config getInstance() {
        return INSTANCE;
    }

    @BooleanConfigEntry(true)
    public boolean separateCamera = true;

    @BooleanConfigEntry(true)
    public boolean useThirdPersonByDefault = true;

    @BooleanConfigEntry(true)
    public boolean enableTrails = true;

    @BooleanConfigEntry(true)
    public boolean enableAnimatedSails = true;

    @FloatConfigEntry(192.0f)
    public float renderDistance;

    @FloatConfigEntry(1.0f)
    public float fuelConsumption;

    @FloatConfigEntry(1.0f)
    public float windClearWeather;

    @FloatConfigEntry(3.0f)
    public float windRainWeather;

    @FloatConfigEntry(3.0f)
    public float windThunderWeather;

    @BooleanConfigEntry(true)
    public boolean collisionDamage;

    @BooleanConfigEntry(true)
    public boolean onlyPlayerCanDestroyAircraft;

    @BooleanConfigEntry(false)
    public boolean burnFuelInCreative;

    @BooleanConfigEntry(true)
    public boolean acceptVanillaFuel;

    @BooleanConfigEntry(true)
    public boolean useCustomKeybindSystem;

    public Map<String, Integer> fuelList = Map.of("minecraft:blaze_powder", 1200);
}
