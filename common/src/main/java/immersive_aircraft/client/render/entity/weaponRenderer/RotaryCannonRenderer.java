package immersive_aircraft.client.render.entity.weaponRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import immersive_aircraft.Main;
import immersive_aircraft.client.render.entity.MeshRenderer;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.weapons.RotaryCannon;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class RotaryCannonRenderer extends WeaponRenderer<RotaryCannon> {
    static final ResourceLocation ID = Main.locate("objects/rotary_cannon.obj");
    static final ResourceLocation TEXTURE = Main.locate("textures/entity/rotary_cannon.png");

    @Override
    public <T extends AircraftEntity> void render(T entity, RotaryCannon weapon, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float tickDelta) {
        matrixStack.pushPose();
        matrixStack.mulPoseMatrix(weapon.getMount().transform());

        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.entityCutout(TEXTURE));

        MeshRenderer.renderObject(MeshRenderer.getFaces(ID, "cube"), matrixStack, vertexConsumer, light);

        matrixStack.translate(0.0f, 12.5f / 16.0f, -4.0f / 16.0f);
        matrixStack.mulPose(weapon.getHeadTransform(tickDelta));
        matrixStack.translate(0.0f, -12.5f / 16.0f, 4.0f / 16.0f);
        MeshRenderer.renderObject(MeshRenderer.getFaces(ID, "head"), matrixStack, vertexConsumer, light);

        matrixStack.popPose();
    }
}
