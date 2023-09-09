package immersive_aircraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Supplier;
import net.minecraft.client.KeyMapping;

public class FallbackKeyBinding extends KeyMapping {
    public Supplier<KeyMapping> fallbackKey;

    public FallbackKeyBinding(String translationKey, InputConstants.Type type, Supplier<KeyMapping> fallbackKey, String category) {
        super(translationKey, type, InputConstants.UNKNOWN.getValue(), category);

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
