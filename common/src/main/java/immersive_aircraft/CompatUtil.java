package immersive_aircraft;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class CompatUtil {
    @ExpectPlatform
    public static boolean isModLoaded(String modid) {
        throw new AssertionError();
    }
}
