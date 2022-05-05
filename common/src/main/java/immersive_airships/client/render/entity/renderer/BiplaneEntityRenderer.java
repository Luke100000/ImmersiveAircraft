package immersive_airships.client.render.entity.renderer;

import immersive_airships.Main;
import immersive_airships.client.render.entity.model.BiplaneEntityModel;
import immersive_airships.entity.AirshipEntity;
import immersive_airships.entity.BiplaneEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.CompositeEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class BiplaneEntityRenderer<T extends BiplaneEntity> extends AirshipEntityRenderer<T> {
    private final Identifier texture;
    private final BiplaneEntityModel<T> model;

    public BiplaneEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.8f;

        model = new BiplaneEntityModel<>(BiplaneEntityModel.getTexturedModelData().createModel());
        texture = Main.locate("textures/entity/biplane.png");
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(T AirshipEntity) {
        return texture;
    }

    @Override
    CompositeEntityModel<T> getModel(AirshipEntity entity) {
        return model;
    }
}
