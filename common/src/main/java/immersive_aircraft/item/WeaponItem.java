package immersive_aircraft.item;

import immersive_aircraft.entity.misc.WeaponMount;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;

public class WeaponItem extends DescriptionItem {
    private final WeaponMount.Type mountType;

    public WeaponItem(Properties settings, WeaponMount.Type mountType) {
        super(settings);

        this.mountType = mountType;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag flags) {
        consumer.accept(Component.translatable("item.immersive_aircraft.item.weapon").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, ctx, display, consumer, flags);
    }

    public WeaponMount.Type getMountType() {
        return mountType;
    }
}
