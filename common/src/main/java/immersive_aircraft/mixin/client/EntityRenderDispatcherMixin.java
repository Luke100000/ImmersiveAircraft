package immersive_aircraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "renderHitbox(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;F)V", at = @At("HEAD"))
    private static void ia$inject$renderHitbox(PoseStack matrixStack, VertexConsumer buffer, Entity entity, float partialTicks, CallbackInfo ci) {
        if (entity instanceof VehicleEntity vehicle) {
            for (AABB aABB : vehicle.getAdditionalShapes()) {
                LevelRenderer.renderLineBox(matrixStack, buffer, aABB.move(-entity.getX(), -entity.getY(), -entity.getZ()), 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }
}
