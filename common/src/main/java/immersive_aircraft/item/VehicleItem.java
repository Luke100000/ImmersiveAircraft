package immersive_aircraft.item;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class VehicleItem extends DescriptionItem {
    private static final Predicate<Entity> RIDERS = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);

    public interface VehicleConstructor {
        VehicleEntity create(Level world);
    }

    private final VehicleConstructor constructor;

    public VehicleItem(Properties settings, VehicleConstructor constructor) {
        super(settings);

        this.constructor = constructor;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(world, user, ClipContext.Fluid.ANY);
        if (((HitResult) hitResult).getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemStack);
        }

        // Anti collision or something
        Vec3 vec3d = user.getViewVector(1.0f);
        List<Entity> list = world.getEntities(user, user.getBoundingBox().expandTowards(vec3d.scale(5.0)).inflate(1.0), RIDERS);
        if (!list.isEmpty()) {
            Vec3 vec3d2 = user.getEyePosition();
            for (Entity entity : list) {
                AABB box = entity.getBoundingBox().inflate(entity.getPickRadius());
                if (!box.contains(vec3d2)) continue;
                return InteractionResultHolder.pass(itemStack);
            }
        }

        // Place the vehicle
        if (((HitResult) hitResult).getType() == HitResult.Type.BLOCK) {
            VehicleEntity entity = constructor.create(world);

            entity.fromItemStack(itemStack);

            entity.setPos(hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z);
            entity.setYRot(user.getYRot());

            if (!world.noCollision(entity, entity.getBoundingBox())) {
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
