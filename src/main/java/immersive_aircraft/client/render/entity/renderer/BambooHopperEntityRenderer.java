package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.BambooHopperEntity;
import immersive_aircraft.resources.BBModelBridge;
import immersive_aircraft.util.obj.Mesh;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Bamboo Hopper renderer, bbmodel-based. Animations mirror the 1.20.1 animation
 * variables: propellers spin with engine_rotation, rudders/flap follow the controls.
 */
public class BambooHopperEntityRenderer<T extends BambooHopperEntity> extends AircraftEntityRenderer<T> {
    private static final ResourceLocation id = Main.locate("objects/bamboo_hopper.bbmodel");

    private final ResourceLocation texture = Main.locate("textures/entity/bamboo_hopper.png");

    private static final Set<String> ANIMATED = new HashSet<>(Arrays.asList(
            "propellor_center", "propellor_left", "propellor_right", "left_rudder", "right_rudder", "flap"
    ));

    private final Model model = new Model()
            .add(
                    new Object(id, "static").setRenderConsumer(
                            (entity, tickDelta) -> {
                                bindTexture(getTexture(entity));
                                GlStateManager.enableCull();
                                for (String key : BBModelBridge.getSubtreeMeshKeys(id, BBModelBridge.ROOT_BONE)) {
                                    if (!ANIMATED.contains(key)) {
                                        renderObject(BBModelBridge.getMesh(id, key), 1.0f, 1.0f, 1.0f, 1.0f);
                                    }
                                }
                            }
                    )
            )
            .add(
                    new Object(id, "propellor_center").setAnimationConsumer(
                            (entity, yaw, tickDelta) -> rotateAroundPivot("propellor_center", entity.engineRotation.getSmooth(tickDelta) * 60.0f, 0.0f, 0.0f, 1.0f)
                    )
            )
            .add(
                    new Object(id, "propellor_left").setAnimationConsumer(
                            (entity, yaw, tickDelta) -> rotateAroundPivot("propellor_left", entity.engineRotation.getSmooth(tickDelta) * 100.0f, 0.0f, 0.0f, 1.0f)
                    )
            )
            .add(
                    new Object(id, "propellor_right").setAnimationConsumer(
                            (entity, yaw, tickDelta) -> rotateAroundPivot("propellor_right", -entity.engineRotation.getSmooth(tickDelta) * 100.0f, 0.0f, 0.0f, 1.0f)
                    )
            )
            .add(
                    new Object(id, "left_rudder").setAnimationConsumer(
                            (entity, yaw, tickDelta) -> rotateAroundPivot("left_rudder", entity.pressingInterpolatedX.getSmooth(tickDelta) * 20.0f, 0.0f, 1.0f, 0.0f)
                    )
            )
            .add(
                    new Object(id, "right_rudder").setAnimationConsumer(
                            (entity, yaw, tickDelta) -> rotateAroundPivot("right_rudder", entity.pressingInterpolatedX.getSmooth(tickDelta) * 20.0f, 0.0f, 1.0f, 0.0f)
                    )
            )
            .add(
                    new Object(id, "flap").setAnimationConsumer(
                            (entity, yaw, tickDelta) -> rotateAroundPivot("flap", entity.pressingInterpolatedZ.getSmooth(tickDelta) * 20.0f, 1.0f, 0.0f, 0.0f)
                    )
            );

    public BambooHopperEntityRenderer(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 2.0f;
    }

    private void rotateAroundPivot(String bone, float degrees, float ax, float ay, float az) {
        float[] pivot = BBModelBridge.getPivot(id, bone);
        if (pivot != null) {
            GlStateManager.translate(pivot[0], pivot[1], pivot[2]);
            GlStateManager.rotate(degrees, ax, ay, az);
            GlStateManager.translate(-pivot[0], -pivot[1], -pivot[2]);
        }
    }

    @Override
    public ResourceLocation getTexture(T entity) {
        return texture;
    }

    @Override
    protected Model getModel(AircraftEntity entity) {
        return model;
    }

    @Override
    protected immersive_aircraft.compat.Vec3f getPivot(AircraftEntity entity) {
        return new immersive_aircraft.compat.Vec3f(0.0f, 0.0f, 0.0f);
    }
}
