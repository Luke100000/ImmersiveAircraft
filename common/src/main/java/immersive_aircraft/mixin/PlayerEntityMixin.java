package immersive_aircraft.mixin;

import immersive_aircraft.entity.AircraftEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends Entity {
    public PlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "shouldDismount", at = @At("HEAD"), cancellable = true)
    void shouldDismountInjection(CallbackInfoReturnable<Boolean> cir) {
        if (getRootVehicle() instanceof AircraftEntity) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "updatePose()V", at = @At("TAIL"))
    void updatePostInjection(CallbackInfo ci) {
        if (getRootVehicle() instanceof AircraftEntity) {
            this.setPose(EntityPose.STANDING);
        }
    }
}
