package immersive_aircraft.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.client.VehicleCameraTransform;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private Entity entity;

    @Shadow
    private Vec3 position;

    @Shadow
    private Frustum cullFrustum;

    @Shadow
    protected abstract float getMaxZoom(float maxZoom);

    @Shadow
    protected abstract void move(float x, float y, float z);

    @Shadow
    public abstract boolean isDetached();

    @Shadow
    public abstract float xRot();

    @Shadow
    public abstract float yRot();

    @Shadow
    public abstract float getCameraEntityPartialTicks(DeltaTracker deltaTracker);

    @Shadow
    public abstract Matrix4f getViewRotationMatrix(Matrix4f matrix);

    @Shadow
    private Matrix4f createProjectionMatrixForCulling() {
        throw new AssertionError();
    }

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    public void ia$alignWithEntity(float tickDelta, CallbackInfo ci) {
        if (isDetached() && entity != null && entity.getVehicle() instanceof VehicleEntity vehicle) {
            move(-getMaxZoom((float) vehicle.getZoom()), 0.0f, 0.0f);
        }
    }

    @Inject(method = "update", at = @At("TAIL"))
    public void ia$updateCullFrustum(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (entity == null || isDetached() || !(entity.getRootVehicle() instanceof VehicleEntity vehicle)) {
            return;
        }

        float partialTicks = getCameraEntityPartialTicks(deltaTracker);
        PoseStack poseStack = new PoseStack();
        VehicleCameraTransform.apply(poseStack, partialTicks, entity, vehicle, xRot(), yRot());

        Matrix4f projection = createProjectionMatrixForCulling().mul(poseStack.last().pose());
        cullFrustum = new Frustum(getViewRotationMatrix(new Matrix4f()), projection);
        cullFrustum.prepare(position.x, position.y, position.z);
    }
}
