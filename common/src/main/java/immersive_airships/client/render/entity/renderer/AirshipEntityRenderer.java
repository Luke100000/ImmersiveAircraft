package immersive_airships.client.render.entity.renderer;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import immersive_airships.Main;
import immersive_airships.client.render.entity.model.AirshipEntityModel;
import immersive_airships.entity.AirshipEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

import java.util.Map;
import java.util.stream.Stream;

public class AirshipEntityRenderer extends EntityRenderer<AirshipEntity> {
    private final Map<AirshipEntity.Type, Pair<Identifier, AirshipEntityModel>> texturesAndModels;

    public AirshipEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
        this.texturesAndModels = Stream.of(AirshipEntity.Type.values()).collect(ImmutableMap.toImmutableMap(type -> type, boatType -> Pair.of(Main.locate("textures/entity/airship.png"), new AirshipEntityModel(
                AirshipEntityModel.getTexturedModelData().createModel()
        ))));
    }

    @Override
    public void render(AirshipEntity AirshipEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.translate(0.0, 0.375, 0.0);
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f - f));
        float h = (float)AirshipEntity.getDamageWobbleTicks() - g;
        float j = AirshipEntity.getDamageWobbleStrength() - g;
        if (j < 0.0f) {
            j = 0.0f;
        }
        if (h > 0.0f) {
            matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(MathHelper.sin(h) * h * j / 10.0f * (float)AirshipEntity.getDamageWobbleSide()));
        }
        if (!MathHelper.approximatelyEquals(AirshipEntity.interpolateBubbleWobble(g), 0.0f)) {
            matrixStack.multiply(new Quaternion(new Vec3f(1.0f, 0.0f, 1.0f), AirshipEntity.interpolateBubbleWobble(g), true));
        }
        Pair<Identifier, AirshipEntityModel> pair = this.texturesAndModels.get(AirshipEntity.getBoatType());
        Identifier identifier = pair.getFirst();
        AirshipEntityModel AirshipEntityModel = pair.getSecond();
        matrixStack.scale(-1.0f, -1.0f, 1.0f);
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90.0f));
        AirshipEntityModel.setAngles(AirshipEntity, g, 0.0f, -0.1f, 0.0f, 0.0f);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(AirshipEntityModel.getLayer(identifier));
        AirshipEntityModel.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
        matrixStack.pop();
        super.render(AirshipEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(AirshipEntity AirshipEntity) {
        return this.texturesAndModels.get(AirshipEntity.getBoatType()).getFirst();
    }
}

