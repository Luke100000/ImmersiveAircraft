package immersive_aircraft.mixin;

import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of the 1.16 {@code PlayerEntityMixin}: {@code shouldDismount -> false} plus
 * {@code updatePose -> STANDING}.
 * <p>
 * 1.12.2 has neither method; both effects come from one place, {@link EntityPlayer#updateRidden()}:
 * <pre>if (!world.isRemote &amp;&amp; isSneaking() &amp;&amp; isRiding()) { dismountRidingEntity(); setSneaking(false); }</pre>
 * Clearing the flag before that runs keeps the rider aboard and out of the sneaking pose. This
 * matters because the mod binds "throttle down" to the sneak key, so without it every throttle
 * decrease ejects the pilot.
 */
@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase {
    public EntityPlayerMixin(World world) {
        super(world);
    }

    @Inject(method = "updateRidden", at = @At("HEAD"))
    private void immersiveAircraft$stayAboard(CallbackInfo ci) {
        if (isSneaking() && getRidingEntity() instanceof VehicleEntity) {
            setSneaking(false);
        }
    }
}
