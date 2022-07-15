package immersive_aircraft.client.render.entity.renderer;

import com.mojang.datafixers.util.Pair;
import immersive_aircraft.client.render.entity.model.AircraftEntityModel;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.util.Utils;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.CompositeEntityModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

import java.util.LinkedList;
import java.util.List;

public abstract class AircraftEntityRenderer<T extends AircraftEntity> extends EntityRenderer<T> {
    public AircraftEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.translate(0.0, 0.375, 0.0);
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0f - yaw));
        float h = (float)entity.getDamageWobbleTicks() - tickDelta;
        float j = entity.getDamageWobbleStrength() - tickDelta;
        if (j < 0.0f) {
            j = 0.0f;
        }
        if (h > 0.0f) {
            matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(MathHelper.sin(h) * h * j / 10.0f * (float)entity.getDamageWobbleSide()));
        }

        float WIND = entity.location == AircraftEntity.Location.IN_AIR ? 0.5f : 0.0f;
        float nx = (float)(Utils.cosNoise((entity.age + tickDelta) / 20.0)) * WIND;
        float ny = (float)(Utils.cosNoise((entity.age + tickDelta) / 21.0)) * WIND;

        Identifier identifier = getTexture(entity);
        CompositeEntityModel<T> AircraftEntityModel = getModel(entity);
        matrixStack.scale(-1.0f, -1.0f, 1.0f);
        matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(entity.getPitch(tickDelta) + ny));
        matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90.0f));
        matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(entity.getRoll(tickDelta) + nx));
        AircraftEntityModel.setAngles(entity, tickDelta, 0.0f, -0.1f, 0.0f, 0.0f);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(AircraftEntityModel.getLayer(identifier));
        AircraftEntityModel.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);

        List<Pair<BannerPattern, DyeColor>> list = new LinkedList<>();
        list.add(new Pair<>(BannerPattern.CREEPER, DyeColor.RED));

        getModel(entity).getBannerParts().forEach(part -> BannerBlockEntityRenderer.renderCanvas(matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV, part, ModelLoader.BANNER_BASE, false, list));

        matrixStack.pop();

        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, i);
    }

    abstract AircraftEntityModel<T> getModel(AircraftEntity entity);
}

