package immersive_aircraft.client.render.entity.renderer.bullet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import immersive_aircraft.Main;
import immersive_aircraft.client.render.entity.renderer.utils.DeferredRenderBuffer;
import immersive_aircraft.entity.bullet.BulletEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

public class BulletEntityRenderer<T extends BulletEntity> extends EntityRenderer<T, BulletEntityRenderer.BulletRenderState> {
    private static final Identifier TEXTURE = Main.locate("textures/entity/bullet.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutout(TEXTURE);

    public BulletEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public BulletRenderState createRenderState() {
        return new BulletRenderState();
    }

    @Override
    public void extractRenderState(T entity, BulletRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.scale = entity.getScale();
        state.light = getPackedLightCoords(entity, tickDelta);
    }

    @Override
    public void submit(BulletRenderState state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        DeferredRenderBuffer buffer = new DeferredRenderBuffer();
        matrixStack.pushPose();
        matrixStack.scale(state.scale, state.scale, state.scale);
        matrixStack.translate(0.0, 0.5, 0.0);
        matrixStack.mulPose(this.entityRenderDispatcher.camera.rotation());
        matrixStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        PoseStack.Pose pose = matrixStack.last();
        Matrix4f matrix4f = pose.pose();
        VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);
        vertex(vertexConsumer, matrix4f, pose, state.light, 0.0f, 0.0f, 0.0f, 1.0f);
        vertex(vertexConsumer, matrix4f, pose, state.light, 1.0f, 0.0f, 1.0f, 1.0f);
        vertex(vertexConsumer, matrix4f, pose, state.light, 1.0f, 1.0f, 1.0f, 0.0f);
        vertex(vertexConsumer, matrix4f, pose, state.light, 0.0f, 1.0f, 0.0f, 0.0f);
        matrixStack.popPose();
        buffer.submit(collector);
        super.submit(state, matrixStack, collector, cameraState);
    }

    private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, PoseStack.Pose pose, int light, float x, float y, float u, float v) {
        vertexConsumer.addVertex(matrix4f, x - 0.5f, y - 0.5f, 0.0f)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    public Identifier getTextureLocation(T entity) {
        return TEXTURE;
    }

    public static class BulletRenderState extends EntityRenderState {
        public float scale;
        public int light;
    }
}
