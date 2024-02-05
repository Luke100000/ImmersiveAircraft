package immersive_aircraft.item;

import immersive_aircraft.entity.misc.WeaponMount;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WeaponItem extends DescriptionItem {
    private final WeaponMount.Type mountType;

    public WeaponItem(Properties settings, WeaponMount.Type mountType) {
        super(settings);

        this.mountType = mountType;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        tooltip.add(Component.translatable("item.immersive_aircraft.item.weapon").withStyle(ChatFormatting.GRAY));

        super.appendHoverText(stack, world, tooltip, context);
    }

    public WeaponMount.Type getMountType() {
        return mountType;
    }
}
