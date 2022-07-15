package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.client.render.entity.model.AircraftEntityModel;
import immersive_aircraft.client.render.entity.model.BiplaneEntityModel;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.BiplaneEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class BiplaneEntityRenderer<T extends BiplaneEntity> extends AircraftEntityRenderer<T> {
    private final Identifier texture;
    private final AircraftEntityModel<T> model;

    public BiplaneEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.8f;

        model = new BiplaneEntityModel<>(BiplaneEntityModel.getTexturedModelData().createModel(), BiplaneEntityModel.getBannerModelData().createModel());
        texture = Main.locate("textures/entity/biplane.png");
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(T AircraftEntity) {
        return texture;
    }

    @Override
    AircraftEntityModel<T> getModel(AircraftEntity entity) {
        return model;
    }
}
