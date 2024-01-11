package immersive_aircraft.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;

import java.util.List;

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
        DyeColor baseColor = ((BannerItem) banner.getItem()).getColor();

        CompoundTag nbtCompound = BlockItem.getBlockEntityData(banner);
        if (nbtCompound == null || !nbtCompound.contains("Patterns")) {
            return List.of(Pair.of(Registry.BANNER_PATTERN.getHolderOrThrow(BannerPatterns.BASE), baseColor));
        }

        ListTag nbtList = nbtCompound.getList("Patterns", 10);

        return BannerBlockEntity.createPatterns(baseColor, nbtList);
    }

    public static boolean getBooleanElement(JsonObject object, String member) {
        JsonElement element = object.getAsJsonPrimitive(member);
        if (element == null) {
            return false;
        }
        return element.getAsBoolean();
    }

    public static int getIntElement(JsonObject object, String member) {
        return getIntElement(object, member, 0);
    }

    public static int getIntElement(JsonObject object, String member, int defaultValue) {
        JsonElement element = object.getAsJsonPrimitive(member);
        if (element == null) {
            return defaultValue;
        }
        return element.getAsInt();
    }
}
