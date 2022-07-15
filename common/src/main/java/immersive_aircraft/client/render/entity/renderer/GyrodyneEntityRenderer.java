package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.client.render.entity.model.AircraftEntityModel;
import immersive_aircraft.client.render.entity.model.GyrodyneEntityModel;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.GyrodyneEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class GyrodyneEntityRenderer<T extends GyrodyneEntity> extends AircraftEntityRenderer<T> {
    private final Identifier texture;
    private final AircraftEntityModel<T> model;

    public GyrodyneEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.8f;

        model = new GyrodyneEntityModel<>(GyrodyneEntityModel.getTexturedModelData().createModel());
        texture = Main.locate("textures/entity/aircraft.png");
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
