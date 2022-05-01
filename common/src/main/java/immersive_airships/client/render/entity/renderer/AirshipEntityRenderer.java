package immersive_airships.client.render.entity.renderer;

import immersive_airships.entity.AirshipEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.CompositeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

public abstract class AirshipEntityRenderer extends EntityRenderer<AirshipEntity> {
    public AirshipEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(AirshipEntity entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.translate(0.0, 0.375, 0.0);
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f - f));
        float h = (float)entity.getDamageWobbleTicks() - g;
        float j = entity.getDamageWobbleStrength() - g;
        if (j < 0.0f) {
            j = 0.0f;
        }
        if (h > 0.0f) {
            matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(MathHelper.sin(h) * h * j / 10.0f * (float)entity.getDamageWobbleSide()));
        }

        Identifier identifier = getTexture(entity);
        CompositeEntityModel<AirshipEntity> AirshipEntityModel = getModel(entity);
        matrixStack.scale(-1.0f, -1.0f, 1.0f);
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90.0f));
        AirshipEntityModel.setAngles(entity, g, 0.0f, -0.1f, 0.0f, 0.0f);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(AirshipEntityModel.getLayer(identifier));
        AirshipEntityModel.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
        matrixStack.pop();

        super.render(entity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    abstract CompositeEntityModel<AirshipEntity> getModel(AirshipEntity entity);
}

