package immersive_aircraft.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import immersive_aircraft.cobalt.registration.CobaltFuelRegistry;
import immersive_aircraft.config.Config;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public static List<Pair<Holder<BannerPattern>, DyeColor>> parseBannerItem(ItemStack banner) {
        if (!(banner.getItem() instanceof BannerItem)) {
            return List.of();
        }

        BannerPatternLayers layers = banner.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        List<Pair<Holder<BannerPattern>, DyeColor>> patterns = new ArrayList<>();
        for (BannerPatternLayers.Layer layer : layers.layers()) {
            patterns.add(Pair.of(layer.pattern(), layer.color()));
        }
        return patterns;
    }

    public static int getFuelTime(ItemStack fuel) {
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
            int fuelTime = CobaltFuelRegistry.INSTANCE.get(fuel);
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
