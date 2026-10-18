package immersive_aircraft.mixin.client;

import immersive_aircraft.entity.PassengerPosture;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.PassengerPositionDescriptor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {
    @Unique
    private boolean immersiveAircraft$passengerCrouching;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
    private void setPassengerPosture(LivingEntity entity, float limbSwing, float limbAmount, float age, float headYaw, float headPitch, CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>) (Object) this;
        if (entity.getVehicle() instanceof VehicleEntity vehicle) {
            PassengerPositionDescriptor position = vehicle.getPassengerPosition(entity);
            PassengerPosture posture = position == null ? PassengerPosture.SEATED : position.posture();
            model.riding = posture == PassengerPosture.SEATED;
            model.crouching = posture == PassengerPosture.CROUCHING;
        } else if (immersiveAircraft$passengerCrouching) {
            model.crouching = entity.isCrouching();
        }

        immersiveAircraft$passengerCrouching = entity.getVehicle() instanceof VehicleEntity && model.crouching;
    }
}
