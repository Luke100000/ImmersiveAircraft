package immersive_aircraft.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface KeyBindingAccessorMixin {
    @Accessor
    int getTimesPressed();

    @Accessor
    void setTimesPressed(int v);
}
