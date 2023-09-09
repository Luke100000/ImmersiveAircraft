package immersive_aircraft.client;

import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Supplier;
import net.minecraft.client.KeyMapping;

public class FallbackKeyBinding extends KeyMapping {
    public Supplier<KeyMapping> fallbackKey;

    public FallbackKeyBinding(String translationKey, InputConstants.Type type, Supplier<KeyMapping> fallbackKey, String category) {
        super(translationKey, type, GLFW.GLFW_KEY_UNKNOWN, category);

        this.fallbackKey = fallbackKey;
    }

    @Override
    public boolean isDown() {
        if (isUnbound()) {
            return fallbackKey.get().isDown();
        } else {
            return super.isDown();
        }
    }

    @Override
    public boolean consumeClick() {
        if (isUnbound()) {
            return fallbackKey.get().consumeClick();
        } else {
            return super.consumeClick();
        }
    }
}
