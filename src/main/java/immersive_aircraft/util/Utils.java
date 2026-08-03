package immersive_aircraft.util;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BannerPattern;

import java.util.Collections;
import java.util.List;

public class Utils {
    public static double cosNoise(double time) {
        return cosNoise(time, 5);
    }

    // 1.12.2 MathHelper has no lerp
    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public static double cosNoise(double time, int layers) {
        double value = 0.0f;
        for (int i = 0; i < layers; i++) {
            value += Math.cos(time);
            time *= 1.3;
        }
        return value;
    }

    // TODO(port): banner pattern rendering. 1.12.2 stores patterns as {Pattern: String, Color: int}
    //  NBT entries resolvable via TileEntityBanner / BannerPattern; the 1.16 code returned
    //  List<Pair<BannerPattern, DyeColor>>. Left unimplemented until the renderers that consume
    //  it (Airship/Biplane/CargoAirship) are fully ported.
    public static List<BannerPattern> parseBannerItem(ItemStack banner) {
        return Collections.emptyList();
    }
}
