package immersive_aircraft.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import immersive_aircraft.client.render.entity.renderer.utils.BBModelRenderer;
import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.resources.BBModelLoader;
import immersive_aircraft.resources.bbmodel.AnimationVariableName;
import immersive_aircraft.resources.bbmodel.BBModel;
import immersive_aircraft.resources.bbmodel.BBObject;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public abstract class VehicleEntityRenderer<T extends VehicleEntity, S extends VehicleEntityRenderState> extends EntityRenderer<T, S> {

    public VehicleEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    protected abstract ModelPartRenderHandler<S> getModel();

    protected abstract ResourceLocation getModelId();

    public void render(S entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();

        // Rotation
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityRenderState.yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(entityRenderState.xRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(entityRenderState.zRot));

        // Render model, weapons, etc.
        renderLocal(entityRenderState, poseStack, submitNodeCollector, getModel());

        poseStack.popPose();
    }

    public void renderLocal(S entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, ModelPartRenderHandler<S> model) {
        //Wobble
        float h = entityRenderState.damageWobbleTicks;
        float j = Math.max(0f, entityRenderState.damageWobbleStrength);
        if (h > 0.0f) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(h) * h * j / 10.0f * (float) entityRenderState.damageWobbleSide));
        }

        // Render model
        BBModel bbModel = BBModelLoader.MODELS.get(getModelId());
        if (bbModel != null) {
            float health = entityRenderState.health;
            float r = health * 0.6f + 0.4f;
            float g = health * 0.4f + 0.6f;
            float b = health * 0.4f + 0.6f;
            BBModelRenderer.renderModel(bbModel, model, poseStack, entityRenderState, submitNodeCollector, r, g, b, 1.0f);
        }
    }

    public void renderOptionalObject(String name, BBModel model, PoseStack matrixStack, S entity, SubmitNodeCollector submitNodeCollector) {
        renderOptionalObject(name, model, matrixStack, entity, submitNodeCollector, 1f, 1f, 1f, 1f);
    }

    public void renderOptionalObject(String name, BBModel model, PoseStack matrixStack, S entity, SubmitNodeCollector submitNodeCollector, float red, float green, float blue, float alpha) {
        BBObject object = model.objectsByName.get(name);
        if (object != null) {
            BBModelRenderer.renderObject(model, object, matrixStack, entity, submitNodeCollector, null, red, green, blue, alpha);
        }
    }

    @Override
    public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        if (!entity.shouldRender(x, y, z)) {
            return false;
        }
        return frustum.isVisible(getBoundingBoxForCulling(entity));
    }

    @Override
    protected @NotNull AABB getBoundingBoxForCulling(T entity) {
        return entity.getBoundingBoxForCulling().inflate(getCullingBoundingBoxInflation());
    }

    @Override
    public void submit(S entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        super.submit(entityRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public void extractRenderState(T entity, S entityRenderState, float f) {
        super.extractRenderState(entity, entityRenderState, f);
        entityRenderState.yRot = entity.getViewYRot(f);
        entityRenderState.xRot = entity.getViewXRot(f);
        entityRenderState.zRot = entity.getRoll(f);
        entityRenderState.damageWobbleSide = entity.getDamageWobbleSide();
        entityRenderState.damageWobbleTicks = entity.getDamageWobbleTicks() - f;
        entityRenderState.damageWobbleStrength = entity.getDamageWobbleStrength() - f;
        entityRenderState.health = entity.getHealth();
        entityRenderState.isWithinParticleRange = entity.isWithinParticleRange();
        entityRenderState.packedLight = this.getPackedLightCoords(entity, f);
        entityRenderState.passengers.clear();
        entityRenderState.passengers.addAll(entity.getPassengers());
        entityRenderState.onGround = entity.onGround();
        entityRenderState.speedVector = entity.getSpeedVector();

        entityRenderState.controllingPassenger = entity.getControllingPassenger();

        entityRenderState.time = (entity.level().getGameTime() % 24000 + f) / 20.0f;
        entityRenderState.animationVariables.set(AnimationVariableName.TIME, entityRenderState.time);
        entity.setAnimationVariables(entityRenderState.animationVariables, f);
    }

    protected double getCullingBoundingBoxInflation() {
        return 1.0;
    }

    private static final ResourceLocation TEXTURE = ResourceLocation.parse("invalid");

}

