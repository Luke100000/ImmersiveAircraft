package immersive_aircraft.client.render.entity.renderer.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.resources.bbmodel.BBModel;
import immersive_aircraft.resources.bbmodel.BBObject;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides a way to provide overrides for rendering and animating model parts.
 */
public class ModelPartRenderHandler<T extends Entity> {
    private final Map<String, ModelPartRenderer<T>> objects = new HashMap<>();
    private BBModelRenderer.RenderTypeProvider renderTypeProvider = BBModelRenderer.DEFAULT_RENDER_TYPE_PROVIDER;

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
            o.animationConsumer().run(entity, 0, time, matrixStack);
        }
    }

    public boolean render(String name, BBModel model, BBObject object, SubmitNodeCollector collector, T entity, PoseStack matrixStack, int light, float time, ModelPartRenderHandler<T> modelPartRenderer) {
        ModelPartRenderer<T> o = objects.get(name);
        if (o != null && o.renderConsumer() != null) {
            o.renderConsumer().run(model, object, collector, entity, matrixStack, light, time, modelPartRenderer);
            return true;
        }
        return false;
    }

    /**
     * Set the render type provider for this model part renderer, allowing for custom render types.
     * @param renderTypeProvider The mapping between renderable and render types.
     */
    public ModelPartRenderHandler<T> renderTypeProvider(BBModelRenderer.RenderTypeProvider renderTypeProvider) {
        this.renderTypeProvider = renderTypeProvider;
        return this;
    }

    public BBModelRenderer.RenderTypeProvider getRenderTypeProvider() {
        return renderTypeProvider;
    }
}
