package immersive_aircraft.client.render.entity.weaponRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import immersive_aircraft.client.render.entity.MeshRenderer;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.weapons.Weapon;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public abstract class SimpleWeaponRenderer<W extends Weapon> extends WeaponRenderer<W> {
    @Override
    public <T extends AircraftEntity> void render(T entity, W weapon, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float tickDelta) {
        matrixStack.pushPose();
        matrixStack.mulPoseMatrix(weapon.getMount().transform());

        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.entityCutout(getTexture()));

        MeshRenderer.renderObject(MeshRenderer.getFaces(getModelId(), "cube"), matrixStack, vertexConsumer, light);

        matrixStack.popPose();
    }

    abstract ResourceLocation getModelId();

    abstract ResourceLocation getTexture();
}
