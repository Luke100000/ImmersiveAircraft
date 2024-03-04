package immersive_aircraft.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import immersive_aircraft.Main;
import immersive_aircraft.WeaponRendererRegistry;
import immersive_aircraft.client.render.entity.renderer.utils.BBModelRenderer;
import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_aircraft.client.render.entity.weaponRenderer.WeaponRenderer;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.entity.weapons.Weapon;
import immersive_aircraft.resources.BBModelLoader;
import immersive_aircraft.resources.bbmodel.*;
import immersive_aircraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;

public abstract class AircraftEntityRenderer<T extends AircraftEntity> extends EntityRenderer<T> {
    public AircraftEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    protected abstract ModelPartRenderHandler<T> getModel(AircraftEntity entity);

    protected abstract ResourceLocation getModelId();

    protected Vector3f getPivot(AircraftEntity entity) {
        return new Vector3f(0.0f, 0.0f, 0.0f);
    }


    @Override
    public void render(T entity, float yaw, float tickDelta, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light) {
        PoseStack.Pose peek = matrixStack.last();

        matrixStack.pushPose();

        //Wobble
        float h = (float) entity.getDamageWobbleTicks() - tickDelta;
        float j = entity.getDamageWobbleStrength() - tickDelta;
        if (j < 0.0f) {
            j = 0.0f;
        }
        if (h > 0.0f) {
            matrixStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(h) * h * j / 10.0f * (float) entity.getDamageWobbleSide()));
        }

        Vector3f effect = entity.isOnGround() ? new Vector3f(0.0f, 0.0f, 0.0f) : entity.getWindEffect();
        matrixStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        matrixStack.mulPose(Axis.XP.rotationDegrees(entity.getViewXRot(tickDelta) + effect.z));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(entity.getRoll(tickDelta) + effect.x));

        Vector3f pivot = getPivot(entity);
        matrixStack.translate(pivot.x, pivot.y, pivot.z);

        // Updated variables
        float time = (entity.getLevel().getGameTime() % 24000 + tickDelta) / 20.0f;
        BBAnimationVariables.set("time", time);
        entity.setAnimationVariables(tickDelta);

        // Render model
        BBModel bbModel = BBModelLoader.MODELS.get(getModelId());
        if (bbModel != null) {
            float health = entity.getHealth();
            float r = health * 0.6f + 0.4f;
            float g = health * 0.4f + 0.6f;
            float b = health * 0.4f + 0.6f;
            BBModelRenderer.renderModel(bbModel, matrixStack, vertexConsumerProvider, light, time, entity, getModel(entity), r, g, b, 1.0f);
        }

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

        //Render trails
        entity.getTrails().forEach(t -> TrailRenderer.render(t, vertexConsumerProvider, peek));

        matrixStack.popPose();

        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
    }

    public void renderOptionalObject(String name, BBModel model, MultiBufferSource vertexConsumerProvider, T entity, PoseStack matrixStack, int light, float time) {
        renderOptionalObject(name, model, vertexConsumerProvider, entity, matrixStack, light, time, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void renderOptionalObject(String name, BBModel model, MultiBufferSource vertexConsumerProvider, T entity, PoseStack matrixStack, int light, float time, float red, float green, float blue, float alpha) {
        BBObject object = model.objectsByName.get(name);
        if (object != null) {
            BBModelRenderer.renderObject(model, object, matrixStack, vertexConsumerProvider, light, time, entity, null, red, green, blue, alpha);
        }
    }

    @Override
    public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        if (!entity.shouldRender(x, y, z)) {
            return false;
        }
        AABB box = entity.getBoundingBoxForCulling().inflate(1.0);
        return frustum.isVisible(box);
    }

    private static final ResourceLocation TEXTURE = new ResourceLocation("invalid");

    @Override
    public ResourceLocation getTextureLocation(@NotNull T aircraft) {
        return TEXTURE;
    }

    public void renderBanners(BBModel model, BBObject ignoredObject, MultiBufferSource vertexConsumerProvider, T entity, PoseStack matrixStack, int light, float time, ModelPartRenderHandler<T> modelPartRenderer) {
        List<ItemStack> slots = entity.getSlots(VehicleInventoryDescription.SlotType.BANNER);
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
        ItemStack stack = entity.getSlots(VehicleInventoryDescription.SlotType.DYE).get(0);
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

