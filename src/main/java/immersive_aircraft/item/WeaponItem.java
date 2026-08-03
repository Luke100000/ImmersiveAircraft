package immersive_aircraft.item;

import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.util.FlowingText;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class WeaponItem extends Item {
    private final WeaponMount.Type mountType;

    public WeaponItem(WeaponMount.Type mountType) {
        this.mountType = mountType;
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.GRAY + I18n.format("item.immersive_aircraft.item.weapon"));

        tooltip.addAll(FlowingText.wrap(TextFormatting.ITALIC + "" + TextFormatting.GRAY + I18n.format(getTranslationKey(stack) + ".description"), 180));
    }

    public WeaponMount.Type getMountType() {
        return mountType;
    }
}
