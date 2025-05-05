package immersive_aircraft.item;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(world, user, onWater ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE);
        if (((HitResult) hitResult).getType() == HitResult.Type.MISS) {
            error(user, "immersive_aircraft.tooltip.no_target");
            return InteractionResultHolder.pass(itemStack);
        }

        // Place the vehicle
        if (((HitResult) hitResult).getType() == HitResult.Type.BLOCK) {
            VehicleEntity entity = constructor.create(world);

            entity.fromItemStack(itemStack);

            entity.setPos(hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z);
            entity.setYRot(user.getYRot());

            if (!world.noCollision(entity, entity.getBoundingBox())) {
                error(user, "immersive_aircraft.tooltip.no_space");
                return InteractionResultHolder.fail(itemStack);
            }

            if (!world.isClientSide) {
                world.addFreshEntity(entity);
                world.gameEvent(user, GameEvent.ENTITY_PLACE, BlockPos.containing(hitResult.getLocation()));
                if (!user.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }

            user.awardStat(Stats.ITEM_USED.get(this));

            return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
        }

        return InteractionResultHolder.pass(itemStack);
    }

    private static void error(Player user, String message) {
        user.displayClientMessage(Component.translatable(message).withStyle(ChatFormatting.RED), true);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);

        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.contains("Inventory")) {
                ListTag nbtList = tag.getList("Inventory", 10);
                tooltip.add(Component.translatable("immersive_aircraft.tooltip.inventory", nbtList.size()));
            }
        }
    }
}
