package immersive_aircraft.mixin;

import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessorMixin {
    @Accessor
    int getTimesPressed();

    @Accessor
    void setTimesPressed(int v);
}
