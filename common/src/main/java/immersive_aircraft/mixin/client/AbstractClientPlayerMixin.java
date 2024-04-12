package immersive_aircraft.mixin.client;

import com.mojang.authlib.GameProfile;
import immersive_aircraft.entity.InventoryVehicleEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player {
    public AbstractClientPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method = "getFieldOfViewModifier()F", at = @At("HEAD"), cancellable = true)
    public void ia$getFieldOfViewModifier(CallbackInfoReturnable<Float> cir) {
        if (getRootVehicle() instanceof InventoryVehicleEntity vehicle && vehicle.isScoping()) {
            cir.setReturnValue(0.05f);
        }
    }
}
