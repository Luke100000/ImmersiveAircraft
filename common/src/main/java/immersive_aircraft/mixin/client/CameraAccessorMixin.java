package immersive_aircraft.mixin.client;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessorMixin {
    @Invoker("calculateFov")
    float immersiveAircraft$calculateFov(float tickDelta);
}
