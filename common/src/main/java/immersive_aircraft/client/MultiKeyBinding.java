package immersive_aircraft.client;

import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public class MultiKeyBinding extends KeyMapping {
    public final InputConstants.Key customDefaultKey;
    public InputConstants.Key customBoundKey;

    public static final Map<InputConstants.Key, List<MultiKeyBinding>> KEY_TO_BINDING = new HashMap<>();

    public MultiKeyBinding(String translationKey, InputConstants.Type type, int code, String category) {
        super(translationKey, type, GLFW.GLFW_KEY_UNKNOWN, category);

        // Avoid overwriting other keys
        InputConstants.Key key = type.getOrCreate(code);
        customDefaultKey = customBoundKey = key;

        KEY_TO_BINDING.computeIfAbsent(customBoundKey, v -> new LinkedList<>()).add(this);
    }

    @Override
    public String saveString() {
        return customBoundKey.getName();
    }

    @Override
    public Component getTranslatedKeyMessage() {
        return customBoundKey.getDisplayName();
    }

    @Override
    public InputConstants.Key getDefaultKey() {
        return customDefaultKey;
    }

    @Override
    public void setKey(InputConstants.Key boundKey) {
        customBoundKey = boundKey;
    }

    @Override
    public boolean isUnbound() {
        return customBoundKey.equals(InputConstants.UNKNOWN);
    }

    @Override
    public boolean isDefault() {
        return customBoundKey.equals(customDefaultKey);
    }
}
