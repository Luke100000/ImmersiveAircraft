package immersive_aircraft.util;

import net.minecraft.block.entity.BannerPattern;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Pair;

import java.util.LinkedList;
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

    public static List<Pair<BannerPattern, DyeColor>> parseBannerItem(ItemStack banner) {
        NbtCompound nbtCompound = BlockItem.getBlockEntityNbt(banner);
        if (nbtCompound == null || !nbtCompound.contains("Patterns")) {
            return List.of();
        }

        NbtList nbtList = nbtCompound.getList("Patterns", 10);
        List<Pair<BannerPattern, DyeColor>> patterns = new LinkedList<>();
        for (int i = 0; i < nbtList.size() && i < 6; ++i) {
            NbtCompound element = nbtList.getCompound(i);
            DyeColor dyeColor = DyeColor.byId(element.getInt("Color"));
            BannerPattern bannerPattern = BannerPattern.byId(element.getString("Pattern"));
            if (bannerPattern == null) continue;
            patterns.add(new Pair<>(bannerPattern, dyeColor));
        }
        return patterns;
    }
}
