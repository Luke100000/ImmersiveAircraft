package immersive_aircraft;

import immersive_aircraft.network.MessageHandler;
import net.minecraft.resources.ResourceLocation;
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

    public static float frameTime = 0.0f;

    static {
        License.iConfirmNonCommercialUse("Conczin");
    }

    public static ResourceLocation locate(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public interface CameraGetter {
        Vec3 getPosition();
    }

    public interface FirstPersonGetter {
        boolean isFirstPerson();
    }
}
