package immersive_aircraft.client;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MultiKeyBinding extends KeyBinding {
    public final InputUtil.Key customDefaultKey;
    public InputUtil.Key customBoundKey;

    public static final Map<InputUtil.Key, List<MultiKeyBinding>> KEY_TO_BINDING = new HashMap<>();

    public MultiKeyBinding(String translationKey, InputUtil.Type type, int code, String category) {
        super(translationKey, type, GLFW.GLFW_KEY_UNKNOWN, category);

        // Avoid overwriting other keys
        InputUtil.Key key = type.createFromCode(code);
        customDefaultKey = customBoundKey = key;

        KEY_TO_BINDING.computeIfAbsent(customBoundKey, v -> new LinkedList<>()).add(this);
    }

    @Override
    public String getBoundKeyTranslationKey() {
        return customBoundKey.getTranslationKey();
    }

    @Override
    public Text getBoundKeyLocalizedText() {
        return customBoundKey.getLocalizedText();
    }

    @Override
    public InputUtil.Key getDefaultKey() {
        return customDefaultKey;
    }

    @Override
    public void setBoundKey(InputUtil.Key boundKey) {
        customBoundKey = boundKey;
    }

    @Override
    public boolean isUnbound() {
        return customBoundKey.equals(InputUtil.UNKNOWN_KEY);
    }

    @Override
    public boolean isDefault() {
        return customBoundKey.equals(customDefaultKey);
    }
}
