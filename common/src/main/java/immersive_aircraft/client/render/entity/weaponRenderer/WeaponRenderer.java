package immersive_aircraft.client.render.entity.weaponRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.client.render.entity.renderer.utils.BBModelRenderer;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.weapon.Weapon;
import immersive_aircraft.resources.BBModelLoader;
import immersive_aircraft.resources.bbmodel.BBModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public abstract class WeaponRenderer<W extends Weapon> {
    public <T extends VehicleEntity> void render(T entity, W weapon, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float time) {
        matrixStack.pushPose();
        matrixStack.mulPoseMatrix(weapon.getMount().transform());

        BBModel model = BBModelLoader.MODELS.get(getModelId());
        weapon.setAnimationVariables(entity, time);
        BBModelRenderer.renderModel(model, matrixStack, vertexConsumerProvider, light, time, entity, null, 1.0f, 1.0f, 1.0f, 1.0f);

        matrixStack.popPose();
    }

    protected abstract ResourceLocation getModelId();
}
