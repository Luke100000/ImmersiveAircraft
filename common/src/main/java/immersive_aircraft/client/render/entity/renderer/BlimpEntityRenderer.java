package immersive_aircraft.client.render.entity.renderer;

import com.mojang.math.Vector3f;
import immersive_aircraft.Main;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.BlimpEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BlimpEntityRenderer<T extends BlimpEntity> extends AircraftEntityRenderer<T> {
    private static final ResourceLocation id = Main.locate("objects/blimp.obj");

    private final ResourceLocation texture = Main.locate("textures/entity/blimp.png");

    private final Model model = new Model()
            .add(new Object(id, "cockpit"))
            .add(new Object(id, "weapon_deck"))
            .add(new Object(id, "frame"))
            .add(new Object(id, "thruster_mount"))
            .add(new Object(id, "blade"))
            .add(new Object(id, "trim"))
            .add(new Object(id, "thruster"))
            .add(new Object(id, "pipe"))
            .add(new Object(id, "back_fin"));

    public BlimpEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }

    @Override
    protected Model getModel(AircraftEntity entity) {
        return model;
    }

    @Override
    protected Vector3f getPivot(AircraftEntity entity) {
        return new Vector3f(0.0f, 0.2f, 0.0f);
    }
}
