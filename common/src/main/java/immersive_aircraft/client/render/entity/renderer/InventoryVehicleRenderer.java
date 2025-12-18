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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class InventoryVehicleRenderer<T extends InventoryVehicleEntity, S extends InventoryVehicleEntityRenderState> extends DyeableVehicleEntityRenderer<T, S> {
    public InventoryVehicleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void extractRenderState(T entity, S entityRenderState, float f) {
        super.extractRenderState(entity, entityRenderState, f);
        entityRenderState.weapons.clear();
        entityRenderState.banners.clear();
        entityRenderState.sailDyes.clear();
        entityRenderState.weapons.addAll(entity.getWeapons().values().stream().flatMap(List::stream).toList());
        entityRenderState.banners.addAll(entity.getSlots(VehicleInventoryDescription.BANNER));
        entityRenderState.sailDyes.addAll(entity.getSlots(VehicleInventoryDescription.DYE));
    }

    @Override
    public void renderLocal(S entityRenderState,
                            PoseStack poseStack,
                            SubmitNodeCollector submitNodeCollector,
                            ModelPartRenderHandler<S> model,
                            CameraRenderState cameraRenderState) {
        super.renderLocal(entityRenderState, poseStack, submitNodeCollector, model, cameraRenderState);
        //Render weapons
        LocalPlayer player = Minecraft.getInstance().player;
        for (Weapon weapon : entityRenderState.weapons) {
            if (!weapon.getMount().blocking() || !Main.firstPersonGetter.isFirstPerson() || player == null || !entityRenderState.passengers.contains(player)) {
                WeaponRenderer<Weapon> renderer = WeaponRendererRegistry.get(weapon);
                if (renderer != null) {
                    renderer.render(entityRenderState, weapon, poseStack, submitNodeCollector);
                }
            }
        }
    }

    public void renderBanners(BBModel model,
                              BBObject ignoredObject,
                              SubmitNodeCollector submitNodeCollector,
                              S entity,
                              PoseStack matrixStack,
                              ModelPartRenderHandler<S> ignoredModelPartRenderer) {
        int i = 0;
        for (ItemStack slot : entity.banners) {
            if (!slot.isEmpty() && slot.getItem() instanceof BannerItem bannerItem) {
                DyeColor baseColor = bannerItem.getColor();
                BannerPatternLayers banner = slot.get(DataComponents.BANNER_PATTERNS);
                if (banner != null) {
                    BBObject bannerObject = model.objectsByName.get("banner_" + (i++));
                    if (bannerObject instanceof BBFaceContainer bannerContainer) {
                        BBModelRenderer.renderBanner(bannerContainer, matrixStack, submitNodeCollector, entity.packedLight, true, baseColor, banner.layers());
                    }
                }
            }
        }
    }

    public void renderSails(BBObject object,
                            SubmitNodeCollector submitNodeCollector,
                            S entity,
                            PoseStack matrixStack) {
        ItemStack stack = entity.sailDyes.stream().findFirst().orElse(ItemStack.EMPTY);
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
            BBModelRenderer.renderSailObject(mesh, matrixStack, submitNodeCollector, entity.packedLight, entity.time, r, g, b, 1.0f);
        }
    }
}
