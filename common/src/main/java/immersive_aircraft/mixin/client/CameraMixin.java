package immersive_aircraft.mixin.client;

import com.mojang.math.Axis;
import immersive_aircraft.client.MouseFlight;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"))
    private void immersiveAircraft$mouseFlightView(BlockGetter area, Entity entity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (thirdPerson && entity instanceof Player player && player.isLocalPlayer()
            && entity.getRootVehicle() instanceof VehicleEntity vehicle && MouseFlight.isEnabled(vehicle)) {
            Camera camera = (Camera) (Object) this;
            setRotation(camera.getYRot(), camera.getXRot() + MouseFlight.getCameraPitchOffset(camera, tickDelta));
        }
    }

    @Inject(method = "setup", at = @At("TAIL"))
    public void ia$setup(BlockGetter area, Entity entity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (thirdPerson && entity.getVehicle() instanceof VehicleEntity vehicle) {
            move(-getMaxZoom((float) vehicle.getZoom()), 0.0f, 0.0f);
        } else if (!thirdPerson && entity.getRootVehicle() instanceof VehicleEntity vehicle) {
            Camera camera = (Camera) (Object) this;
            if (entity instanceof Player player && player.isLocalPlayer() && MouseFlight.isEnabled(vehicle)) {
                setRotation(camera.getYRot(), camera.getXRot() + MouseFlight.getCameraPitchOffset(camera, tickDelta));
            } else if (vehicle.adaptPlayerRotation) {
                setRotation(camera.getYRot(), camera.getXRot() + vehicle.getViewXRot(tickDelta));
                Quaternionf rotation = camera.rotation().mul(Axis.ZP.rotationDegrees(-vehicle.getRoll(tickDelta)));
                camera.getLookVector().set(0.0f, 0.0f, -1.0f).rotate(rotation);
                camera.getUpVector().set(0.0f, 1.0f, 0.0f).rotate(rotation);
                camera.getLeftVector().set(-1.0f, 0.0f, 0.0f).rotate(rotation);
            }

            float eye = entity.getEyeHeight();
            Quaternionf vehicleRotation = new Quaternionf().rotationYXZ(
                    -vehicle.getViewYRot(tickDelta) * Mth.DEG_TO_RAD,
                    vehicle.getViewXRot(tickDelta) * Mth.DEG_TO_RAD,
                    vehicle.getRoll(tickDelta) * Mth.DEG_TO_RAD
            );
            Vector3f offset = new Vector3f(0.0f, eye, 0.0f).rotate(vehicleRotation);
            setPosition(camera.getPosition().add(offset.x(), offset.y() - eye, offset.z()));
        }
    }

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Shadow
    protected abstract void move(float zoom, float dy, float dx);

    @Shadow
    protected abstract float getMaxZoom(float maxZoom);
}