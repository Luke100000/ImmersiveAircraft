package immersive_aircraft.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

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
                BannerPatternLayers banner = slot.get(DataComponents.BANNER_PATTERNS);
                if (banner != null) {
                    BBObject bannerObject = model.objectsByName.get("banner_" + (i++));
                    if (bannerObject instanceof BBFaceContainer bannerContainer) {
                        BBModelRenderer.renderBanner(bannerContainer, matrixStack, vertexConsumerProvider, light, true, banner.layers());
                    }
                }
            }
        }
    }

    public void renderSails(BBObject object, MultiBufferSource vertexConsumerProvider, T entity, PoseStack matrixStack, int light, float time) {
        ItemStack stack = entity.getSlots(VehicleInventoryDescription.DYE).get(0);
        DyeColor color;
        if (stack.getItem() instanceof DyeItem item) {
            color = item.getDyeColor();
        } else {
            color = DyeColor.WHITE;
        }
        int c = color.getTextureDiffuseColor();
        float r = ((c >> 16) & 0xFF) / 255.0f;
        float g = ((c >> 8) & 0xFF) / 255.0f;
        float b = (c & 0xFF) / 255.0f;

        if (object instanceof BBMesh mesh) {
            BBModelRenderer.renderSailObject(mesh, matrixStack, vertexConsumerProvider, light, time, r, g, b, 1.0f);
        }
    }
}
