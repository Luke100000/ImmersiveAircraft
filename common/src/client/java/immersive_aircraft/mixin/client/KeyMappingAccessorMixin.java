package immersive_aircraft.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface KeyMappingAccessorMixin {
    @Accessor
    int getClickCount();

    @Accessor
    void setClickCount(int v);

    @Accessor
    InputConstants.Key getKey();
}
