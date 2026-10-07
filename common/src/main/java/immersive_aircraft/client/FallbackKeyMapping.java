package immersive_aircraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import immersive_aircraft.mixin.client.KeyMappingAccessorMixin;
import net.minecraft.client.KeyMapping;

import java.util.function.Supplier;

public class FallbackKeyMapping extends KeyMapping {
    public final Supplier<KeyMapping> fallbackKey;

    public FallbackKeyMapping(String translationKey, InputConstants.Type type, Supplier<KeyMapping> fallbackKey, KeyMapping.Category category) {
        super(translationKey, type, InputConstants.UNKNOWN.getValue(), category);

        this.fallbackKey = fallbackKey;
    }

    @Override
    public boolean isDown() {
        KeyMapping fallback = getFallbackKey();
        return super.isDown() || (fallback != null && fallback.isDown());
    }

    @Override
    public boolean consumeClick() {
        boolean clicked = super.consumeClick();
        KeyMapping fallback = getFallbackKey();
        if (fallback != null) {
            clicked |= fallback.consumeClick();
        }
        return clicked;
    }

    public KeyMapping getFallbackKey() {
        return isDefault() ? fallbackKey.get() : null;
    }

    public InputConstants.Key getBoundKey() {
        KeyMapping fallback = getFallbackKey();
        return ((KeyMappingAccessorMixin) (fallback != null ? fallback : this)).getKey();
    }
}
