package immersive_aircraft.config;

import immersive_aircraft.Main;
import net.minecraftforge.common.config.Config;

/**
 * Forge annotation-driven config (run/config/immersive_aircraft.cfg), editable from the
 * in-game mod list via {@link ConfigGuiFactory}. Values are mirrored into the
 * {@link Config#getInstance()} singleton used by the rest of the mod — see {@link Config#sync()}.
 */
@Config(modid = Main.MOD_ID, name = "immersive_aircraft", category = "general")
public class ForgeConfig {
    @Config.Comment("Use a custom, non-vanilla camera behavior while flying")
    @Config.Name("Separate camera")
    public static boolean separateCamera = true;

    @Config.Comment("Switch to third person when entering an aircraft")
    @Config.Name("Third person by default")
    public static boolean useThirdPersonByDefault = true;

    @Config.Comment("Render trails behind propellers and wings")
    @Config.Name("Enable trails")
    public static boolean enableTrails = true;

    @Config.Comment("Animate the sails of airships")
    @Config.Name("Animated sails")
    public static boolean enableAnimatedSails = true;

    @Config.Comment("Maximum distance in blocks at which aircraft are rendered")
    @Config.Name("Render distance")
    @Config.RangeDouble(min = 0)
    public static float renderDistance = 192.0f;

    @Config.Comment("Global fuel consumption multiplier")
    @Config.Name("Fuel consumption")
    @Config.RangeDouble(min = 0)
    public static float fuelConsumption = 1.0f;

    @Config.Comment("Wind strength in clear weather")
    @Config.Name("Wind (clear)")
    @Config.RangeDouble(min = 0)
    public static float windClearWeather = 1.0f;

    @Config.Comment("Wind strength in rain")
    @Config.Name("Wind (rain)")
    @Config.RangeDouble(min = 0)
    public static float windRainWeather = 3.0f;

    @Config.Comment("Wind strength in thunderstorms")
    @Config.Name("Wind (thunder)")
    @Config.RangeDouble(min = 0)
    public static float windThunderWeather = 3.0f;

    @Config.Comment("Aircraft take damage when colliding with blocks")
    @Config.Name("Collision damage")
    public static boolean collisionDamage = true;

    @Config.Comment("Only players can destroy aircraft (empty aircraft are invulnerable)")
    @Config.Name("Only players destroy aircraft")
    public static boolean onlyPlayerCanDestroyAircraft = true;

    @Config.Comment("Burn fuel even when the pilot is in creative mode")
    @Config.Name("Burn fuel in creative")
    public static boolean burnFuelInCreative = false;

    @Config.Comment("Accept vanilla furnace fuels in boilers")
    @Config.Name("Accept vanilla fuel")
    public static boolean acceptVanillaFuel = true;

    @Config.Comment("Use the custom keybind system (multi-key bindings) instead of vanilla movement keys")
    @Config.Name("Custom keybind system")
    @Config.RequiresMcRestart
    public static boolean useCustomKeybindSystem = true;

    @Config.Comment("Damage multiplier applied to the pilot when an aircraft crashes")
    @Config.Name("Crash damage")
    @Config.RangeDouble(min = 0)
    public static float crashDamage = 2.0f;

    @Config.Comment("Crash damage never kills the pilot (leaves half a heart)")
    @Config.Name("Prevent kill through crash")
    public static boolean preventKillThroughCrash = true;

    @Config.Name("fuel")
    public static Fuel fuel = new Fuel();

    public static class Fuel {
        @Config.Comment("Additional fuels, one per line, in the form modid:itemid=burnTimeInTicks (e.g. minecraft:lava_bucket=20000)")
        @Config.Name("Fuel list")
        public String[] fuelList = new String[0];
    }
}
