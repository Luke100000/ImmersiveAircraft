package immersive_aircraft.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import immersive_aircraft.Main;
import immersive_aircraft.WeaponRendererRegistry;
import immersive_aircraft.client.render.entity.renderer.utils.BBModelRenderer;
import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_aircraft.client.render.entity.weaponRenderer.WeaponRenderer;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.inventory.VehicleInventoryDescription;
import immersive_aircraft.entity.weapon.Weapon;
import immersive_aircraft.resources.bbmodel.BBFaceContainer;
import immersive_aircraft.resources.bbmodel.BBMesh;
import immersive_aircraft.resources.bbmodel.BBModel;
import immersive_aircraft.resources.bbmodel.BBObject;
import immersive_aircraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Holder;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;

import java.util.List;

public abstract class InventoryVehicleRenderer<T extends InventoryVehicleEntity> extends DyeableVehicleEntityRenderer<T> {
    public InventoryVehicleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void renderLocal(T entity, float yaw, float tickDelta, PoseStack matrixStack, PoseStack.Pose peek, MultiBufferSource vertexConsumerProvider, int light) {
        super.renderLocal(entity, yaw, tickDelta, matrixStack, peek, vertexConsumerProvider, light);

        //Render weapons
        LocalPlayer player = Minecraft.getInstance().player;
        for (List<Weapon> weapons : entity.getWeapons().values()) {
            for (Weapon weapon : weapons) {
                if (!weapon.getMount().blocking() || !Main.firstPersonGetter.isFirstPerson() || player == null || !entity.hasPassenger(player)) {
                    WeaponRenderer<Weapon> renderer = WeaponRendererRegistry.get(weapon);
                    if (renderer != null) {
                        renderer.render(entity, weapon, matrixStack, vertexConsumerProvider, light, tickDelta);
                    }
                }
            }
        }
    }

    public void renderBanners(BBModel model, BBObject ignoredObject, MultiBufferSource vertexConsumerProvider, T entity, PoseStack matrixStack, int light, float ignoredTime, ModelPartRenderHandler<T> ignoredModelPartRenderer) {
        List<ItemStack> slots = entity.getSlots(VehicleInventoryDescription.BANNER);
        int i = 0;
        for (ItemStack slot : slots) {
            if (!slot.isEmpty() && slot.getItem() instanceof BannerItem) {
                List<Pair<Holder<BannerPattern>, DyeColor>> patterns = Utils.parseBannerItem(slot);
                BBObject bannerObject = model.objectsByName.get("banner_" + (i++));
                if (bannerObject instanceof BBFaceContainer bannerContainer) {
                    BBModelRenderer.renderBanner(bannerContainer, matrixStack, vertexConsumerProvider, light, true, patterns);
                }
            }
        }
    }

    public void renderSails(BBObject object, MultiBufferSource vertexConsumerProvider, T entity, PoseStack matrixStack, int light, float time) {
        List<ItemStack> slots = entity.getSlots(VehicleInventoryDescription.DYE);
        ItemStack stack = slots.stream().findFirst().orElse(ItemStack.EMPTY);
        DyeColor color;
        if (stack.getItem() instanceof DyeItem item) {
            color = item.getDyeColor();
        } else {
            color = DyeColor.WHITE;
        }
        float r = color.getTextureDiffuseColors()[0];
        float g = color.getTextureDiffuseColors()[1];
        float b = color.getTextureDiffuseColors()[2];

        if (object instanceof BBMesh mesh) {
            BBModelRenderer.renderSailObject(mesh, matrixStack, vertexConsumerProvider, light, time, r, g, b, 1.0f);
        }
    }
}
