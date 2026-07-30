package immersive_aircraft.mixin.client;

import immersive_aircraft.config.Config;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "setup", at = @At("TAIL"))
    public void ia$setup(Level level, Entity entity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (entity.getVehicle() instanceof VehicleEntity vehicle) {
            // Не отодвигаем камеру, если игрок использует подзорную трубу (scoping)
            boolean isScoping = vehicle instanceof InventoryVehicleEntity invVehicle && invVehicle.isScoping();
            
            // Если hideVehicleWhileScoping включен и игрок смотрит в трубу — не двигаем камеру
            if (isScoping && Config.getInstance().hideVehicleWhileScoping) {
                return;
            }
            
            if (thirdPerson && !isScoping) {
                move(-getMaxZoom((float) vehicle.getZoom()), 0.0f, 0.0f);
            }
        }
    }

    @Shadow
    protected abstract void move(float zoom, float dy, float dx);

    @Shadow
    protected abstract float getMaxZoom(float maxZoom);
}
