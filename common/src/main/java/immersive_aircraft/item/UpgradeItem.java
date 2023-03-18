package immersive_aircraft.item;

import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.item.upgrade.AircraftUpgrade;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UpgradeItem extends Item {
    static final DecimalFormat fmt = new DecimalFormat("+#;-#");

    private final AircraftUpgrade upgrade;

    public UpgradeItem(Settings settings, AircraftUpgrade upgrade) {
        super(settings);

        this.upgrade = upgrade;
    }

    public AircraftUpgrade getUpgrade() {
        return upgrade;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        tooltip.add(Text.translatable("item.immersive_aircraft.item.upgrade").formatted(Formatting.GRAY).formatted(Formatting.ITALIC));

        for (Map.Entry<AircraftStat, Float> entry : getUpgrade().getAll().entrySet()) {
            tooltip.add(Text.translatable("immersive_aircraft.upgrade." + entry.getKey().name().toLowerCase(Locale.ROOT),
                    fmt.format(entry.getValue() * 100)
            ).formatted(entry.getValue() * (entry.getKey().isPositive() ? 1 : -1) > 0 ? Formatting.GREEN : Formatting.RED));
        }
    }
}
