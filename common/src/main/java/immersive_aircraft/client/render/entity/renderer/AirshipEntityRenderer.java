package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.client.render.entity.model.AircraftEntityModel;
import immersive_aircraft.client.render.entity.model.AirshipEntityModel;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.AirshipEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class AirshipEntityRenderer<T extends AirshipEntity> extends AircraftEntityRenderer<T> {
    private final Identifier texture;
    private final AircraftEntityModel<T> model;

    public AirshipEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.8f;

        model = new AirshipEntityModel<>(AirshipEntityModel.getTexturedModelData().createModel());
        texture = Main.locate("textures/entity/airship.png");
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
