package immersive_aircraft.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.architectury.registry.fuel.FuelRegistry;
import immersive_aircraft.cobalt.registration.CobaltFuelRegistry;
import immersive_aircraft.config.Config;
import immersive_aircraft.screen.slot.FuelSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FuelValues;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;
import java.util.function.Supplier;

public class Utils {
    public static double cosNoise(double time) {
        return cosNoise(time, 5);
    }

    public static double cosNoise(double time, int layers) {
        double value = 0.0f;
        for (int i = 0; i < layers; i++) {
            value += Math.cos(time);
            time *= 1.3;
        }
        return value;
    }

    public static int getFuelTime(ItemStack fuel, FuelValues fuelValues) {
        if (fuel.isEmpty()) {
            return 0;
        }

        // Custom fuel
        Map<String, Integer> fuelList = Config.getInstance().fuelList;
        String identifier = BuiltInRegistries.ITEM.getKey(fuel.getItem()).toString();
        if (fuelList.containsKey(identifier)) {
            return fuelList.get(identifier);
        }

        // Vanilla fuel
        if (Config.getInstance().acceptVanillaFuel) {
            int fuelTime = FuelRegistry.get(fuel, null, fuelValues);
            if (fuelTime > 0) {
                return fuelTime;
            }
        }

        return 0;
    }

    public static boolean getBooleanElement(JsonObject object, String member) {
        JsonElement element = object.getAsJsonPrimitive(member);
        if (element == null) {
            return false;
        }
        return element.getAsBoolean();
    }

    public static boolean isNull(JsonObject object, String member) {
        return object.has(member) && object.get(member).isJsonNull();
    }

    public static int getIntElement(JsonObject object, String member) {
        return getIntElement(object, member, 0);
    }

    public static int getIntElement(JsonObject object, String member, int defaultValue) {
        JsonElement element = object.getAsJsonPrimitive(member);
        if (element == null) {
            return defaultValue;
        }
        if (element instanceof JsonPrimitive primitive && primitive.isNumber()) {
            return primitive.getAsInt();
        }
        return defaultValue;
    }

    public static float getFloatElement(JsonObject object, String member) {
        return getFloatElement(object, member, 0);
    }

    public static float getFloatElement(JsonObject object, String member, float defaultValue) {
        JsonElement element = object.getAsJsonPrimitive(member);
        if (element == null) {
            return defaultValue;
        }
        return element.getAsFloat();
    }

    public static Vector3f parseVector(JsonObject element, String member) {
        JsonArray array = element.getAsJsonArray(member);
        if (array == null) {
            return new Vector3f();
        }
        return new Vector3f(
                array.get(0).getAsFloat(),
                array.get(1).getAsFloat(),
                array.get(2).getAsFloat()
        );
    }

    public static Quaternionf fromXYZ(float pitch, float yaw, float roll) {
        Quaternionf quaternion = new Quaternionf();
        quaternion.rotationZYX(roll, yaw, pitch);
        return quaternion;
    }

    public static Quaternionf fromXYZ(Vector3f rotation) {
        return fromXYZ(rotation.x, rotation.y, rotation.z);
    }
}
