package immersive_aircraft.config;

import immersive_aircraft.config.configEntries.BooleanConfigEntry;
import immersive_aircraft.config.configEntries.FloatConfigEntry;
import immersive_aircraft.config.configEntries.IntegerConfigEntry;

import java.util.Map;

public final class Config extends JsonConfig {
    private static final Config INSTANCE = loadOrCreate();

    public static Config getInstance() {
        return INSTANCE;
    }


    // Crash configs
    @BooleanConfigEntry(true)
    public boolean enableDropsForNonPlayer = true;

    @BooleanConfigEntry(true)
    public boolean enableCrashExplosion;

    @BooleanConfigEntry(false)
    public boolean enableCrashBlockDestruction = false;

    @BooleanConfigEntry(false)
    public boolean enableCrashFire = false;

    @FloatConfigEntry(2.0F)
    public float crashExplosionRadius;

    @FloatConfigEntry(2.0f)
    public float crashDamage;

    @BooleanConfigEntry(true)
    public boolean preventKillThroughCrash;

    @IntegerConfigEntry(0)
    public int healthBarRow;

    @IntegerConfigEntry(30)
    public int damagePerHealthPoint;

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

    @BooleanConfigEntry(false)
    public boolean burnFuelInCreative;

    @BooleanConfigEntry(true)
    public boolean acceptVanillaFuel;

    @BooleanConfigEntry(true)
    public boolean useCustomKeybindSystem;

    public Map<String, Integer> fuelList = Map.of(
            "minecraft:blaze_powder", 1200
    );

    public Map<String, Boolean> validDimensions = Map.of(
            "minecraft:overworld", true,
            "minecraft:the_nether", true,
            "minecraft:the_end", true
    );

    public Map<String, Integer> powderAmmunition = Map.of(
            "minecraft:gun_powder", 100
    );

    public Map<String, Integer> arrowAmmunition = Map.of(
            "minecraft:arrow", 100,
            "minecraft:tipped_arrow", 100,
            "minecraft:spectral_arrow", 100
    );
}
