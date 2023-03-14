package immersive_aircraft.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.item.BannerItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.DyeColor;

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
        DyeColor baseColor = ((BannerItem)banner.getItem()).getColor();

        NbtCompound nbtCompound = BlockItem.getBlockEntityNbt(banner);
        if (nbtCompound == null || !nbtCompound.contains("Patterns")) {
            return List.of(Pair.of(BannerPattern.BASE, baseColor));
        }

        NbtList nbtList = nbtCompound.getList("Patterns", 10);

        return BannerBlockEntity.getPatternsFromNbt(baseColor, nbtList);
    }
}
