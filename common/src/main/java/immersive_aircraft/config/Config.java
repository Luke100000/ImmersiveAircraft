package immersive_aircraft.config;

import immersive_aircraft.Main;
import immersive_aircraft.config.configEntries.BooleanConfigEntry;
import immersive_aircraft.config.configEntries.FloatConfigEntry;
import immersive_aircraft.config.configEntries.IntegerConfigEntry;

import java.util.Map;

public final class Config extends JsonConfig {
    private static final Config INSTANCE = loadOrCreate(new Config(Main.MOD_ID), Config.class);

    public Config() {
        super("default");
    }

    public Config(String name) {
        super(name);
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    @Override
    int getVersion() {
        return 3;
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

    @FloatConfigEntry(7.0F)
    public float crashExplosionRadius;

    @FloatConfigEntry(20.0f)
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

    @FloatConfigEntry(320.0f)
    public float renderDistance;

    @FloatConfigEntry(30.0f)
    public float fuelConsumption;

    // Engine acceleration multiplier (1.0 = normal, 2.0 = twice as fast spin-up, 0.5 = twice as slow)
    // Engine spin-down is always 3x slower than spin-up
    @FloatConfigEntry(1.0f)
    public float engineAccelerationMultiplier;

    @FloatConfigEntry(3.0f)
    public float windClearWeather;

    @FloatConfigEntry(10.0f)
    public float windRainWeather;

    @FloatConfigEntry(20.0f)
    public float windThunderWeather;

    @FloatConfigEntry(0.025f)
    public float repairSpeed;

    @FloatConfigEntry(2.0f)
    public float repairExhaustion;

    @BooleanConfigEntry(true)
    public boolean collisionDamage;

    @FloatConfigEntry(40.0f)
    public float collisionDamageMultiplier;

    @BooleanConfigEntry(false)
    public boolean burnFuelInCreative;

    @BooleanConfigEntry(true)
    public boolean acceptVanillaFuel;

    @BooleanConfigEntry(true)
    public boolean useCustomKeybindSystem;

    @BooleanConfigEntry(true)
    public boolean showHotbarEngineGauge;

    @BooleanConfigEntry(false)
    public boolean weaponsAreDestructive;

    @BooleanConfigEntry(true)
    public boolean dropAircraft;

    @BooleanConfigEntry(true)
    public boolean dropInventory;

    @BooleanConfigEntry(false)
    public boolean dropUpgrades;

    @IntegerConfigEntry(0)
    public int regenerateHealthEveryNTicks;

    @BooleanConfigEntry(false)
    public boolean requireShiftForRepair;

    // The entity to spawn when triggering the bomb bay
    // The item also needs to be valid ammunition (e.g., set to 100)
    public Map<String, String> bombBayEntity = Map.of(
            "minecraft:egg", "minecraft:chicken"
    );

    // Weapon settings
    @FloatConfigEntry(4.0f)
    public float rotaryCannonDamage;

    // The velocity also determines the arrow's damage
    @FloatConfigEntry(3.75f)
    public float heavyCrossBowVelocity;

    @FloatConfigEntry(0.3f)
    public float heavyCrossBowInaccuracy;

    @FloatConfigEntry(0.1875f)
    public float heavyCrossBowCooldown;

    // Spread of arrow velocity (0.25 = ±25%)
    @FloatConfigEntry(0.375f)
    public float heavyCrossBowVelocitySpread;

    // Multi-heavy crossbow settings
    @FloatConfigEntry(2.5f)
    public float multiHeavyCrossBowVelocity;

    @FloatConfigEntry(1.0f)
    public float multiHeavyCrossBowInaccuracy;

    @FloatConfigEntry(0.75f)
    public float multiHeavyCrossBowCooldown;

    @IntegerConfigEntry(7)
    public int multiHeavyCrossBowBulletCount;

    // Global vehicle speed multiplier (1.0 = normal, 2.0 = double speed)
    @FloatConfigEntry(1.0f)
    public float globalEngineSpeedMultiplier;

    // Durability multiplier (0.5 = twice as fragile)
    @FloatConfigEntry(0.5f)
    public float durabilityMultiplier;

    // Gravity multiplier (1.0 = normal gravity)
    @FloatConfigEntry(1.0f)
    public float gravityMultiplier;

    @FloatConfigEntry(1.0f)
    public float engineOffDrag;

    // Hide vehicle model when scoping with telescope
    @BooleanConfigEntry(true)
    public boolean hideVehicleWhileScoping;

    public Map<String, Integer> fuelList = Map.of(
            "minecraft:blaze_powder", 1200
    );

    public Map<String, Boolean> validDimensions = Map.of(
            "minecraft:overworld", true,
            "minecraft:the_nether", true,
            "minecraft:the_end", true
    );

    public Map<String, Integer> copperAmmunition = Map.of(
            "minecraft:copper_nugget", 100
    );

    public Map<String, Integer> arrowAmmunition = Map.of(
            "minecraft:arrow", 100,
            "minecraft:tipped_arrow", 100,
            "minecraft:spectral_arrow", 100
    );

    public Map<String, Integer> bombBayAmmunition = Map.of(
            "minecraft:tnt", 100
    );
}