package immersive_aircraft.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessorMixin {
    @Invoker("getFov")
    double immersiveAircraft$getFov(Camera camera, float tickDelta, boolean useFovSetting);
}
