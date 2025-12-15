package immersive_aircraft.client.render.entity.renderer.bullet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import immersive_aircraft.Main;
import immersive_aircraft.entity.bullet.BulletEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class BulletEntityRenderer<T extends BulletEntity> extends EntityRenderer<T, BulletEntityRenderState> {
    private static final ResourceLocation TEXTURE = Main.locate("textures/entity/bullet.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE);

    public BulletEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(BulletEntityRenderState entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        float scale = entityRenderState.scale;
        int packedLight = entityRenderState.packedLight;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0, 0.5, 0.0);
        poseStack.mulPose(cameraRenderState.orientation);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        submitNodeCollector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, vertexConsumer) -> {
            Matrix4f matrix4f = pose.pose();
            Matrix3f matrix3f = pose.normal();
            vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 0.0f, 0.0f, 0.0f, 1.0f);
            vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 1.0f, 0.0f, 1.0f, 1.0f);
            vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 1.0f, 1.0f, 1.0f, 0.0f);
            vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 0.0f, 1.0f, 0.0f, 0.0f);
        });
        poseStack.popPose();
        super.submit(entityRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public @NotNull BulletEntityRenderState createRenderState() {
        return new BulletEntityRenderState();
    }

    @Override
    public void extractRenderState(T entity, BulletEntityRenderState entityRenderState, float f) {
        super.extractRenderState(entity, entityRenderState, f);
        entityRenderState.scale = entity.getScale();
        entityRenderState.packedLight = this.getPackedLightCoords(entity, f);
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
