package immersive_aircraft.mixin;

import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Player.class, priority = 1100)
public abstract class PlayerEntityMixin extends Entity {
    public PlayerEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "wantsToStopRiding", at = @At("HEAD"), cancellable = true)
    void shouldDismountInjection(CallbackInfoReturnable<Boolean> cir) {
        if (this.getRootVehicle() instanceof VehicleEntity) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "updatePlayerPose()V", at = @At("TAIL"))
    void updatePostInjection(CallbackInfo ci) {
        if (getRootVehicle() instanceof VehicleEntity) {
            this.setPose(Pose.STANDING);
        }
    }

    @Inject(method = "isScoping()Z", at = @At("HEAD"), cancellable = true)
    void isScopingInjection(CallbackInfoReturnable<Boolean> cir) {
        if (this.getRootVehicle() instanceof InventoryVehicleEntity vehicle && vehicle.isScoping()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    public void immersive_aircraft$getDestroySpeed(CallbackInfoReturnable<Float> cir) {
        if (this.getRootVehicle() instanceof VehicleEntity) {
            cir.setReturnValue(cir.getReturnValueF() * 5.0f);
        }
    }
}
