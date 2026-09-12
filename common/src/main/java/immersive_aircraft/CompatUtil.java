package immersive_aircraft;

import java.util.Objects;
import java.util.function.Predicate;

public final class CompatUtil {
    private static Predicate<String> isModLoaded = modid -> false;

    private CompatUtil() {
    }

    public static void setModLoadedChecker(Predicate<String> checker) {
        isModLoaded = Objects.requireNonNull(checker);
    }

    public static boolean isModLoaded(String modid) {
        return isModLoaded.test(modid);
    }
}
