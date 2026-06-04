package immersive_aircraft.item;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.function.Consumer;

public class VehicleItem extends DescriptionItem {
    public interface VehicleConstructor {
        VehicleEntity create(Level world);
    }

    private final VehicleConstructor constructor;
    private final boolean onWater;

    public VehicleItem(Properties settings, VehicleConstructor constructor) {
        this(settings, constructor, true);
    }

    public VehicleItem(Properties settings, VehicleConstructor constructor, boolean onWater) {
        super(settings);

        this.constructor = constructor;
        this.onWater = onWater;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(world, user, onWater ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE);
        if (((HitResult) hitResult).getType() == HitResult.Type.MISS) {
            error(user, "immersive_aircraft.tooltip.no_target");
            return InteractionResult.PASS;
        }

        // Place the vehicle
        if (((HitResult) hitResult).getType() == HitResult.Type.BLOCK) {
            VehicleEntity entity = constructor.create(world);

            entity.fromItemStack(itemStack);

            entity.setPos(hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z);
            entity.setYRot(user.getYRot());

            if (!world.noCollision(entity, entity.getBoundingBox())) {
                error(user, "immersive_aircraft.tooltip.no_space");
                return InteractionResult.FAIL;
            }

            if (!world.isClientSide()) {
                world.addFreshEntity(entity);
                world.gameEvent(user, GameEvent.ENTITY_PLACE, BlockPos.containing(hitResult.getLocation()));
                if (!user.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }

            user.awardStat(Stats.ITEM_USED.get(this));

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private static void error(Player user, String message) {
        user.sendOverlayMessage(Component.translatable(message).withStyle(ChatFormatting.RED));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (!customData.isEmpty()) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("Inventory")) {
                ListTag nbtList = tag.getListOrEmpty("Inventory");
                tooltip.accept(Component.translatable("immersive_aircraft.tooltip.inventory", nbtList.size()));
            }
        }

        long containerItems = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItemCopyStream().count();
        if (containerItems > 0) {
            tooltip.accept(Component.translatable("immersive_aircraft.tooltip.inventory", containerItems));
        }
    }
}
