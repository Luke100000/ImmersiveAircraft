package immersive_aircraft.client.render.entity.renderer.bullet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import immersive_aircraft.Main;
import immersive_aircraft.entity.bullet.BulletEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class BulletEntityRenderer<T extends BulletEntity> extends EntityRenderer<T, BulletEntityRenderState> {
    private static final Identifier TEXTURE = Main.locate("textures/entity/bullet.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutoutNoCull(TEXTURE);

    public BulletEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull BulletEntityRenderState createRenderState() {
        return new BulletEntityRenderState();
    }

    @Override
    public void extractRenderState(T entity, BulletEntityRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.scale = entity.getScale();
    }

    @Override
    public void submit(BulletEntityRenderState state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        matrixStack.pushPose();
        float scale = state.scale;
        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(0.0, 0.5, 0.0);
        matrixStack.mulPose(cameraState.orientation);
        matrixStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        PoseStack.Pose pose = matrixStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        collector.submitCustomGeometry(matrixStack, RENDER_TYPE, (poseEntry, vertexConsumer) -> {
            vertex(vertexConsumer, matrix4f, matrix3f, state.lightCoords, 0.0f, 0.0f, 0.0f, 1.0f);
            vertex(vertexConsumer, matrix4f, matrix3f, state.lightCoords, 1.0f, 0.0f, 1.0f, 1.0f);
            vertex(vertexConsumer, matrix4f, matrix3f, state.lightCoords, 1.0f, 1.0f, 1.0f, 0.0f);
            vertex(vertexConsumer, matrix4f, matrix3f, state.lightCoords, 0.0f, 1.0f, 0.0f, 0.0f);
        });

        matrixStack.popPose();
        super.submit(state, matrixStack, collector, cameraState);
    }

    private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, int light, float x, float y, float u, float v) {
        Vector3f n = matrix3f.transform(new Vector3f(0.0f, 1.0f, 0.0f));
        vertexConsumer.addVertex(matrix4f, x - 0.5f, y - 0.5f, 0.0f)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(n.x(), n.y(), n.z());
    }
}
