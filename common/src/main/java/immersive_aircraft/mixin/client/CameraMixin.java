package immersive_aircraft.mixin.client;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private boolean detached;

    @Shadow
    private Entity entity;

    @Shadow
    protected abstract void move(float zoom, float dy, float dx);

    @Shadow
    protected abstract float getMaxZoom(float maxZoom);

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void ia$alignWithEntity(float partialTicks, CallbackInfo ci) {
        if (this.detached && this.entity != null && this.entity.getVehicle() instanceof VehicleEntity vehicle) {
            move(-getMaxZoom((float) vehicle.getZoom()), 0.0f, 0.0f);
        }
    }
}