package immersive_aircraft.client.render.entity.renderer.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;

public abstract class LegacyEntityRenderer<T extends Entity> extends EntityRenderer<T, LegacyEntityRenderer.LegacyRenderState<T>> {
    protected LegacyEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public LegacyRenderState<T> createRenderState() {
        return new LegacyRenderState<>();
    }

    @Override
    public void extractRenderState(T entity, LegacyRenderState<T> state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.entity = entity;
        state.tickDelta = tickDelta;
        state.yaw = entity.getViewYRot(tickDelta);
        state.lightCoords = getPackedLightCoords(entity, tickDelta);
    }

    @Override
    public void submit(LegacyRenderState<T> state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.entity == null) {
            return;
        }
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        renderLegacy(state.entity, state.yaw, state.tickDelta, matrixStack, bufferSource, state.lightCoords, cameraState);
        bufferSource.endBatch();
    }

    protected abstract void renderLegacy(T entity, float yaw, float tickDelta, PoseStack matrixStack, MultiBufferSource buffer, int light, CameraRenderState cameraState);

    public static class LegacyRenderState<T extends Entity> extends EntityRenderState {
        public T entity;
        public float yaw;
        public float tickDelta;
    }
}
