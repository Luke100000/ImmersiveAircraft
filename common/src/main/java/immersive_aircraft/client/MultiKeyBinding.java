package immersive_aircraft.client;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class MultiKeyBinding extends KeyBinding {
    public MultiKeyBinding(String translationKey, InputUtil.Type type, int code, String category) {
        super(translationKey, type, code, category);
    }
}
