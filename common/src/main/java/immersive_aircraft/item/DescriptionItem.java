package immersive_aircraft.item;

import immersive_aircraft.util.FlowingText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;

public abstract class DescriptionItem extends Item {
    public DescriptionItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, TooltipDisplay display, Consumer<Component> tooltips, TooltipFlag flags) {
        super.appendHoverText(stack, ctx, display, tooltips, flags);
        for (Component component : FlowingText.wrap(Component.translatable(getDescriptionId() + ".description").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY), 180)) {
            tooltips.accept(component);
        }
    }
}
