package immersive_aircraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import immersive_aircraft.mixin.client.KeyMappingAccessorMixin;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MultiKeyMapping extends KeyMapping {
    public final InputConstants.Key customDefaultKey;
    public InputConstants.Key customBoundKey;

    public static final Map<InputConstants.Key, List<MultiKeyMapping>> KEY_TO_BINDING = new HashMap<>();

    public MultiKeyMapping(String translationKey, InputConstants.Type type, int code, String category) {
        super(translationKey, type, GLFW.GLFW_KEY_UNKNOWN, category);

        // Avoid overwriting other keys
        InputConstants.Key key = type.getOrCreate(code);
        customDefaultKey = customBoundKey = key;

        KEY_TO_BINDING.computeIfAbsent(customBoundKey, v -> new LinkedList<>()).add(this);
    }

    @Override
    public boolean isDown() {
        validate();
        return super.isDown();
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
    public void setKey(InputConstants.@NotNull Key boundKey) {
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

    /**
     * Forge does some custom loading magic, lets just elevate the vanilla key to custom key if we detect such change
     */
    public void validate() {
        if (!super.isDefault()) {
            customBoundKey = ((KeyMappingAccessorMixin) this).getKey();
            super.setKey(super.getDefaultKey());
            resetMapping();
        }
    }
}
