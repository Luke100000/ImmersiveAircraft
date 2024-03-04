package immersive_aircraft.client.render.entity.renderer.bullet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import immersive_aircraft.Main;
import immersive_aircraft.entity.bullet.BulletEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BulletEntityRenderer<T extends BulletEntity> extends EntityRenderer<T> {
    private static final ResourceLocation TEXTURE = Main.locate("textures/entity/bullet.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE);

    public BulletEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();
        float scale = entity.getScale();
        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(0.0, 0.5, 0.0);
        matrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        matrixStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        PoseStack.Pose pose = matrixStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 0.0f, 0.0f, 0.0f, 1.0f);
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 1.0f, 0.0f, 1.0f, 1.0f);
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 1.0f, 1.0f, 1.0f, 0.0f);
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, 0.0f, 1.0f, 0.0f, 0.0f);
        matrixStack.popPose();
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer vertexConsumer, Matrix4f matrix4f, Matrix3f matrix3f, int light, float x, float y, float u, float v) {
        vertexConsumer.vertex(matrix4f, x - 0.5f, y - 0.5f, 0.0f)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(matrix3f, 0.0f, 1.0f, 0.0f)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }
}
