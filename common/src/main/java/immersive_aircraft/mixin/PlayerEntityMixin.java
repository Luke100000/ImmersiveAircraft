package immersive_aircraft.mixin;

import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends Entity {
    public PlayerEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "wantsToStopRiding", at = @At("HEAD"), cancellable = true)
    void shouldDismountInjection(CallbackInfoReturnable<Boolean> cir) {
        if (this.getRootVehicle() instanceof AircraftEntity) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "updatePlayerPose()V", at = @At("TAIL"))
    void updatePostInjection(CallbackInfo ci) {
        if (getRootVehicle() instanceof AircraftEntity) {
            this.setPose(Pose.STANDING);
        }
    }

    @Inject(method = "isScoping()Z", at = @At("HEAD"), cancellable = true)
    void isScopingInjection(CallbackInfoReturnable<Boolean> cir) {
        if (this.getRootVehicle() instanceof AircraftEntity aircraft && aircraft.isScoping()) {
            cir.setReturnValue(true);
        }
    }

    @Redirect(method = "getDestroySpeed", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;onGround:Z"))
    public boolean immersive_aircraft$patchedGetDestroySpeed(Player player) {
        if (player.getRootVehicle() instanceof VehicleEntity) {
            return true;
        }
        return player.isOnGround();
    }
}
