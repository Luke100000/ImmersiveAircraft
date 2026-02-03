package immersive_aircraft.item;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.LayeredCauldronBlock;

public class DyeableAircraftItem extends AircraftItem {
    public DyeableAircraftItem(Properties settings, AircraftConstructor constructor) {
        super(settings, constructor);

        CauldronInteraction.WATER.map().put(this, (state, level, pos, player, hand, stack) -> {
            if (!stack.has(DataComponents.DYED_COLOR)) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide()) {
                stack.remove(DataComponents.DYED_COLOR);
                LayeredCauldronBlock.lowerFillLevel(state, level, pos);
                player.awardStat(Stats.CLEAN_ARMOR);
            }
            return InteractionResult.SUCCESS;
        });
    }
}
