package immersive_aircraft.item;

import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.util.FlowingText;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class AircraftItem extends Item {
    public interface AircraftConstructor {
        AircraftEntity create(World world);
    }

    private final AircraftConstructor constructor;

    public AircraftItem(AircraftConstructor constructor) {
        this.constructor = constructor;
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);

        tooltip.addAll(FlowingText.wrap(TextFormatting.ITALIC + "" + TextFormatting.GRAY + I18n.format(getTranslationKey(stack) + ".description"), 180));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer user, EnumHand hand) {
        ItemStack itemStack = user.getHeldItem(hand);
        RayTraceResult hitResult = rayTrace(world, user, true);
        if (hitResult == null || hitResult.typeOfHit == RayTraceResult.Type.MISS) {
            return new ActionResult<>(EnumActionResult.PASS, itemStack);
        }

        // Anti collision or something
        Vec3d vec3d = user.getLook(1.0f);
        List<Entity> list = world.getEntitiesInAABBexcluding(user, user.getEntityBoundingBox().expand(vec3d.x * 5.0, vec3d.y * 5.0, vec3d.z * 5.0).grow(1.0),
                entity -> EntitySelectors.NOT_SPECTATING.apply(entity) && entity.canBeCollidedWith());
        if (!list.isEmpty()) {
            Vec3d vec3d2 = user.getPositionEyes(1.0F);
            for (Entity entity : list) {
                AxisAlignedBB box = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
                if (!box.contains(vec3d2)) continue;
                return new ActionResult<>(EnumActionResult.PASS, itemStack);
            }
        }

        // Place the aircraft
        if (hitResult.typeOfHit == RayTraceResult.Type.BLOCK) {
            AircraftEntity entity = constructor.create(world);

            entity.setPosition(hitResult.hitVec.x, hitResult.hitVec.y, hitResult.hitVec.z);
            entity.rotationYaw = user.rotationYaw;

            // transfer a dye color from the item to dyeable vehicles
            if (entity instanceof immersive_aircraft.entity.DyeableVehicleEntity) {
                net.minecraft.nbt.NBTTagCompound display = itemStack.getSubCompound("display");
                if (display != null && display.hasKey("color")) {
                    ((immersive_aircraft.entity.DyeableVehicleEntity) entity).setDyeColor(display.getInteger("color"));
                }
            }

            if (!world.getCollisionBoxes(entity, entity.getEntityBoundingBox().grow(-0.1)).isEmpty()) {
                return new ActionResult<>(EnumActionResult.FAIL, itemStack);
            }

            if (!world.isRemote) {
                world.spawnEntity(entity);
                if (!user.capabilities.isCreativeMode) {
                    itemStack.shrink(1);
                }
            }

            user.addStat(StatList.getObjectUseStats(this));

            return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
        }

        return new ActionResult<>(EnumActionResult.PASS, itemStack);
    }
}
