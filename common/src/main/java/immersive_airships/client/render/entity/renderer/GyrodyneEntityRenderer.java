package immersive_airships.client.render.entity.renderer;

import immersive_airships.Main;
import immersive_airships.client.render.entity.model.AirshipEntityModel;
import immersive_airships.client.render.entity.model.GyrodyneEntityModel;
import immersive_airships.entity.AirshipEntity;
import immersive_airships.entity.GyrodyneEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class GyrodyneEntityRenderer<T extends GyrodyneEntity> extends AirshipEntityRenderer<T> {
    private final Identifier texture;
    private final AirshipEntityModel<T> model;

    public GyrodyneEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.8f;

        model = new GyrodyneEntityModel<>(GyrodyneEntityModel.getTexturedModelData().createModel());
        texture = Main.locate("textures/entity/airship.png");
    }

    @Override
    public Identifier getTexture(T AirshipEntity) {
        return texture;
    }

    @Override
    AirshipEntityModel<T> getModel(AirshipEntity entity) {
        return model;
    }
}
