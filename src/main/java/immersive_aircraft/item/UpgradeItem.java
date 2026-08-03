package immersive_aircraft.item;

import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.item.upgrade.AircraftUpgrade;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UpgradeItem extends Item {
    static final DecimalFormat fmt = new DecimalFormat("+#;-#");

    private final AircraftUpgrade upgrade;

    public UpgradeItem(AircraftUpgrade upgrade) {
        this.upgrade = upgrade;
        setMaxStackSize(8);
    }

    public AircraftUpgrade getUpgrade() {
        return upgrade;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);

        tooltip.add(TextFormatting.GRAY + "" + TextFormatting.ITALIC + I18n.format("item.immersive_aircraft.item.upgrade"));

        for (Map.Entry<AircraftStat, Float> entry : getUpgrade().getAll().entrySet()) {
            tooltip.add((entry.getValue() * (entry.getKey().isPositive() ? 1 : -1) > 0 ? TextFormatting.GREEN : TextFormatting.RED) +
                    I18n.format("immersive_aircraft.upgrade." + entry.getKey().name().toLowerCase(Locale.ROOT),
                            fmt.format(entry.getValue() * 100)));
        }
    }
}
