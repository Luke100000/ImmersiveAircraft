package immersive_aircraft.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import immersive_aircraft.client.render.entity.renderer.utils.BBModelRenderer;
import immersive_aircraft.client.render.entity.renderer.utils.DeferredRenderBuffer;
import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.resources.BBModelLoader;
import immersive_aircraft.resources.bbmodel.BBAnimationVariables;
import immersive_aircraft.resources.bbmodel.BBModel;
import immersive_aircraft.resources.bbmodel.BBObject;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public abstract class VehicleEntityRenderer<T extends VehicleEntity> extends EntityRenderer<T, VehicleEntityRenderer.VehicleRenderState<T>> {
    public VehicleEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    protected abstract ModelPartRenderHandler<T> getModel(VehicleEntity entity);

    protected abstract Identifier getModelId();

    @Override
    public VehicleRenderState<T> createRenderState() {
        return new VehicleRenderState<>();
    }

    @Override
    public void extractRenderState(T entity, VehicleRenderState<T> state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.entity = entity;
        state.yaw = entity.getViewYRot(tickDelta);
        state.tickDelta = tickDelta;
        state.light = getPackedLightCoords(entity, tickDelta);
    }

    @Override
    public void submit(VehicleRenderState<T> state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        T entity = state.entity;
        if (entity == null) {
            return;
        }

        DeferredRenderBuffer vertexConsumerProvider = new DeferredRenderBuffer();
        PoseStack.Pose peek = matrixStack.last();

        matrixStack.pushPose();

        // Rotation
        matrixStack.mulPose(Axis.YP.rotationDegrees(-state.yaw));
        matrixStack.mulPose(Axis.XP.rotationDegrees(entity.getViewXRot(state.tickDelta)));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(entity.getRoll(state.tickDelta)));

        // Render model, weapons, etc.
        renderLocal(entity, state.yaw, state.tickDelta, matrixStack, peek, vertexConsumerProvider, state.light);

        matrixStack.popPose();

        vertexConsumerProvider.submit(collector);
        super.submit(state, matrixStack, collector, cameraState);
    }

    public void renderLocal(T entity, float yaw, float tickDelta, PoseStack matrixStack, PoseStack.Pose peek, MultiBufferSource vertexConsumerProvider, int light) {
        //Wobble
        float h = (float) entity.getDamageWobbleTicks() - tickDelta;
        float j = entity.getDamageWobbleStrength() - tickDelta;
        if (j < 0.0f) {
            j = 0.0f;
        }
        if (h > 0.0f) {
            matrixStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(h) * h * j / 10.0f * (float) entity.getDamageWobbleSide()));
        }

        // Updated variables
        float time = (entity.level().getGameTime() % 24000 + tickDelta) / 20.0f;
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
        AABB box = entity.getBoundingBoxForCulling().inflate(getCullingBoundingBoxInflation());
        return frustum.isVisible(box);
    }

    protected double getCullingBoundingBoxInflation() {
        return 1.0;
    }

    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("invalid");

    public Identifier getTextureLocation(@NotNull T aircraft) {
        return TEXTURE;
    }

    public static class VehicleRenderState<T extends VehicleEntity> extends EntityRenderState {
        public T entity;
        public float yaw;
        public float tickDelta;
        public int light;
    }
}

