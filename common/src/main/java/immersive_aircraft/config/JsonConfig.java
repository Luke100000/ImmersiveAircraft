package immersive_aircraft.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import immersive_aircraft.config.configEntries.BooleanConfigEntry;
import immersive_aircraft.config.configEntries.FloatConfigEntry;
import immersive_aircraft.config.configEntries.IntegerConfigEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class JsonConfig {
    public static final Logger LOGGER = LogManager.getLogger();

    public int version = 0;
    public final String name;

    int getVersion() {
        return 1;
    }

    public JsonConfig(String name) {
        this.name = name;

        for (Field field : this.getClass().getDeclaredFields()) {
            for (Annotation annotation : field.getAnnotations()) {
                try {
                    if (annotation instanceof IntegerConfigEntry entry) {
                        field.setInt(this, entry.value());
                    } else if (annotation instanceof FloatConfigEntry entry) {
                        field.setFloat(this, entry.value());
                    } else if (annotation instanceof BooleanConfigEntry entry) {
                        field.setBoolean(this, entry.value());
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static File getConfigFile(String id) {
        return new File("./config/" + id + ".json");
    }

    public void save() {
        try (FileWriter writer = new FileWriter(getConfigFile(name))) {
            version = getVersion();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(this, writer);
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    public static <T extends JsonConfig> T loadOrCreate(T defaultConfig, Class<T> jsonClass) {
        String name = defaultConfig.name;
        if (getConfigFile(name).exists()) {
            try (FileReader reader = new FileReader(getConfigFile(name))) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                T config = gson.fromJson(reader, jsonClass);
                if (config.version != config.getVersion()) {
                    config = defaultConfig;
                }
                config.save();
                return config;
            } catch (Exception e) {
                LOGGER.error("Failed to load config for '%s'! Default config is used for now. Delete the file to reset.".formatted(name));
                LOGGER.error(e);
                return defaultConfig;
            }
        } else {
            defaultConfig.save();
            return defaultConfig;
        }
    }
}
