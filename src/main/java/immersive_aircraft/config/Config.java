package immersive_aircraft.config;

import immersive_aircraft.Main;

import java.util.HashMap;
import java.util.Map;

/**
 * Runtime mirror of the Forge annotation config ({@link ForgeConfig}). Call sites keep
 * using {@link #getInstance()}; {@link #sync()} copies the @Config statics over and is
 * called once during preInit and on every config change (see {@link ConfigEventHandler}).
 */
public final class Config {
    private static final Config INSTANCE = new Config();

    public static Config getInstance() {
        return INSTANCE;
    }

    public boolean separateCamera = true;

    public boolean useThirdPersonByDefault = true;

    public boolean enableTrails = true;

    public boolean enableAnimatedSails = true;

    public float renderDistance = 192.0f;

    public float fuelConsumption = 1.0f;

    public float windClearWeather = 1.0f;

    public float windRainWeather = 3.0f;

    public float windThunderWeather = 3.0f;

    public boolean collisionDamage = true;

    public boolean onlyPlayerCanDestroyAircraft = true;

    public boolean burnFuelInCreative = false;

    public boolean acceptVanillaFuel = true;

    public boolean useCustomKeybindSystem = true;

    public float crashDamage = 2.0f;

    public boolean preventKillThroughCrash = true;

    public Map<String, Integer> fuelList = new HashMap<>();

    /**
     * Copy the Forge @Config statics into this singleton and parse the fuel list.
     */
    public static void sync() {
        Config c = INSTANCE;

        c.separateCamera = ForgeConfig.separateCamera;
        c.useThirdPersonByDefault = ForgeConfig.useThirdPersonByDefault;
        c.enableTrails = ForgeConfig.enableTrails;
        c.enableAnimatedSails = ForgeConfig.enableAnimatedSails;
        c.renderDistance = ForgeConfig.renderDistance;
        c.fuelConsumption = ForgeConfig.fuelConsumption;
        c.windClearWeather = ForgeConfig.windClearWeather;
        c.windRainWeather = ForgeConfig.windRainWeather;
        c.windThunderWeather = ForgeConfig.windThunderWeather;
        c.collisionDamage = ForgeConfig.collisionDamage;
        c.onlyPlayerCanDestroyAircraft = ForgeConfig.onlyPlayerCanDestroyAircraft;
        c.burnFuelInCreative = ForgeConfig.burnFuelInCreative;
        c.acceptVanillaFuel = ForgeConfig.acceptVanillaFuel;
        c.useCustomKeybindSystem = ForgeConfig.useCustomKeybindSystem;
        c.crashDamage = ForgeConfig.crashDamage;
        c.preventKillThroughCrash = ForgeConfig.preventKillThroughCrash;

        c.fuelList.clear();
        for (String entry : ForgeConfig.fuel.fuelList) {
            int sep = entry.lastIndexOf('=');
            if (sep <= 0 || sep == entry.length() - 1) {
                Main.LOGGER.warn("Malformed fuel list entry (expected modid:itemid=ticks): {}", entry);
                continue;
            }
            try {
                c.fuelList.put(entry.substring(0, sep), Integer.parseInt(entry.substring(sep + 1)));
            } catch (NumberFormatException e) {
                Main.LOGGER.warn("Malformed fuel list entry (invalid burn time): {}", entry);
            }
        }
    }
}
