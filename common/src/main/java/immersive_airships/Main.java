package immersive_airships;

import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Main {
    public static final String MOD_ID = "immersive_airships";
    public static final Logger LOGGER = LogManager.getLogger();

    public static Identifier locate(String path) {
        return new Identifier(MOD_ID, path);
    }
}
