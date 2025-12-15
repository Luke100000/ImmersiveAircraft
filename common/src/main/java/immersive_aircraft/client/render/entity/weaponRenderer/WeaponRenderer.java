package immersive_aircraft.client.render.entity.weaponRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.client.render.entity.renderer.VehicleEntityRenderState;
import immersive_aircraft.client.render.entity.renderer.utils.BBModelRenderer;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.weapon.Weapon;
import immersive_aircraft.resources.BBModelLoader;
import immersive_aircraft.resources.bbmodel.BBModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.ResourceLocation;

public abstract class WeaponRenderer<W extends Weapon> {
    public <T extends VehicleEntityRenderState> void render(T entity, W weapon, PoseStack matrixStack, SubmitNodeCollector submitNodeCollector) {
        matrixStack.pushPose();
        matrixStack.mulPose(weapon.getMount().transform());

        BBModel model = BBModelLoader.MODELS.get(getModelId());
        weapon.setAnimationVariables(entity.animationVariables, entity.time);
        BBModelRenderer.renderModel(model, null, matrixStack, entity, submitNodeCollector, 1.0f, 1.0f, 1.0f, 1.0f);

        matrixStack.popPose();
    }

    protected abstract ResourceLocation getModelId();
}
