package immersive_aircraft.client.render.entity.weaponRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
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
        matrixStack.mulPose(Quaternion.fromYXZ(-weapon.getYaw(), weapon.getPitch(), 0.0f));

        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.entityCutout(TEXTURE));
        MeshRenderer.renderObject(MeshRenderer.getFaces(ID, "cube"), matrixStack, vertexConsumer, light);

        matrixStack.popPose();
    }
}
