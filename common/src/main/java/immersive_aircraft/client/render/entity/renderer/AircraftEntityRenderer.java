package immersive_aircraft.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import immersive_aircraft.client.render.entity.renderer.utils.ModelPartRenderHandler;
import immersive_aircraft.entity.AircraftEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public abstract class AircraftEntityRenderer<T extends AircraftEntity> extends InventoryVehicleRenderer<T, AircraftEntityRenderState> {
    public AircraftEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    // Because this is used in plugins, changing to generic T is no longer possible
    protected abstract ModelPartRenderHandler<AircraftEntityRenderState> getModel();

    @Override
    public void renderLocal(AircraftEntityRenderState entity, PoseStack matrixStack, SubmitNodeCollector submitNodeCollector, ModelPartRenderHandler<AircraftEntityRenderState> modelPartRenderHandler) {
        // Wind effect
        Vector3f effect = entity.onGround ? new Vector3f(0.0f, 0.0f, 0.0f) : entity.windEffect;
        matrixStack.mulPose(Axis.XP.rotationDegrees(effect.z));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(effect.x));

        super.renderLocal(entity, matrixStack, submitNodeCollector, modelPartRenderHandler);

        //Render trails
        entity.trails.forEach(t -> TrailRenderer.render(t, submitNodeCollector, matrixStack));
    }

    @Override
    public @NotNull AircraftEntityRenderState createRenderState() {
        return new AircraftEntityRenderState();
    }

    @Override
    public void extractRenderState(T entity, AircraftEntityRenderState entityRenderState, float f) {
        super.extractRenderState(entity, entityRenderState, f);
        entityRenderState.windEffect = entity.getWindEffect();
        entityRenderState.trails.clear();
        entityRenderState.trails.addAll(entity.getTrails());
        entityRenderState.enginePower = entity.enginePower.getSmooth(f);
        entityRenderState.tickCount = entity.tickCount;


    }
}

