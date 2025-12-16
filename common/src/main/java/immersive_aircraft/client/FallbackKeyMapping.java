package immersive_aircraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

import java.util.function.Supplier;

public class FallbackKeyMapping extends KeyMapping {
    public final Supplier<KeyMapping> fallbackKey;

    public FallbackKeyMapping(String translationKey, InputConstants.Type type, Supplier<KeyMapping> fallbackKey, String category) {
        super(translationKey, type, InputConstants.UNKNOWN.getValue(), KeyBindings.CATEGORY);

        this.fallbackKey = fallbackKey;
    }

    @Override
    public boolean isDown() {
        if (isDefault()) {
            return fallbackKey.get().isDown();
        } else {
            return super.isDown();
        }
    }

    @Override
    public boolean consumeClick() {
        if (isDefault()) {
            return fallbackKey.get().consumeClick();
        } else {
            return super.consumeClick();
        }
    }
}
