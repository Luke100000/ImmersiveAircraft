package immersive_aircraft.client.render.entity.renderer.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.client.render.entity.renderer.VehicleEntityRenderState;
import immersive_aircraft.resources.bbmodel.BBModel;
import immersive_aircraft.resources.bbmodel.BBObject;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a way to provide overrides for rendering and animating model parts.
 */
public class ModelPartRenderHandler<T extends EntityRenderState> {
    private final Map<String, ModelPartRenderer<T>> objects = new HashMap<>();

    public ModelPartRenderHandler<T> add(String id, ModelPartRenderer.AnimationConsumer<T> animationConsumer) {
        return add(id, animationConsumer, null);
    }

    public ModelPartRenderHandler<T> add(String id, ModelPartRenderer.RenderConsumer<T> renderConsumer) {
        return add(id, null, renderConsumer);
    }

    public ModelPartRenderHandler<T> add(String id, ModelPartRenderer.AnimationConsumer<T> animationConsumer, ModelPartRenderer.RenderConsumer<T> renderConsumer) {
        ModelPartRenderer<T> o = new ModelPartRenderer<>(id, animationConsumer, renderConsumer);
        objects.put(o.id(), o);
        return this;
    }

    public Map<String, ModelPartRenderer<T>> getObjects() {
        return objects;
    }

    public void animate(String name, T entity, PoseStack matrixStack, float time) {
        ModelPartRenderer<T> o = objects.get(name);
        if (o != null && o.animationConsumer() != null) {
            o.animationConsumer().run(entity, matrixStack, time);
        }
    }

    public boolean render(String name,
                          BBModel model,
                          BBObject object,
                          SubmitNodeCollector submitNodeCollector,
                          T entity,
                          PoseStack matrixStack,
                          ModelPartRenderHandler<T> modelPartRenderer) {
        ModelPartRenderer<T> o = objects.get(name);
        if (o != null && o.renderConsumer() != null) {
            o.renderConsumer().run(model, object, submitNodeCollector, entity, matrixStack, modelPartRenderer);
            return true;
        }
        return false;
    }

}
