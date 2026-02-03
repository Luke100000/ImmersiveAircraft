package immersive_aircraft;

import immersive_aircraft.network.MessageHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mariuszgromada.math.mxparser.License;

public final class Main {
    public static final String MOD_ID = "immersive_aircraft";
    public static String MOD_LOADER = "unknown";
    public static final Logger LOGGER = LogManager.getLogger();
    public static MessageHandler messageHandler;
    public static CameraGetter cameraGetter = () -> Vec3.ZERO;
    public static FirstPersonGetter firstPersonGetter = () -> false;
    public static DebouncingGetter debouncingGetter = key -> false;
    public static KeyStateGetter keyStateGetter = key -> false;
    public static KeyNameGetter keyNameGetter = key -> Component.empty();
    public static TextWrapper textWrapper = (text, maxWidth) -> java.util.List.of(text);

    public static float frameTime = 0.0f;

    static {
        License.iConfirmNonCommercialUse("Conczin");
    }

    public static Identifier locate(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public interface CameraGetter {
        Vec3 getPosition();
    }

    public interface FirstPersonGetter {
        boolean isFirstPerson();
    }

    public enum Key {
        BOOST,
        DISMOUNT
    }

    public interface DebouncingGetter {
        boolean is(Key keybinding);
    }

    public enum KeyState {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        FORWARD,
        BACKWARD,
        PUSH,
        PULL
    }

    public interface KeyStateGetter {
        boolean isDown(KeyState key);
    }

    public interface KeyNameGetter {
        Component get(Key key);
    }

    public interface TextWrapper {
        java.util.List<Component> wrap(Component text, int maxWidth);
    }
}

