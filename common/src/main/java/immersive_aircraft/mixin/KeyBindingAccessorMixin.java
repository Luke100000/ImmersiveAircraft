package immersive_aircraft.mixin;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessorMixin {
    @Accessor
    InputUtil.Key getBoundKey();

    @Accessor
    int getTimesPressed();

    @Accessor
    void setTimesPressed(int v);
}
