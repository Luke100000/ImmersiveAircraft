package immersive_aircraft.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import immersive_aircraft.Main;
import immersive_aircraft.config.configEntries.BooleanConfigEntry;
import immersive_aircraft.config.configEntries.FloatConfigEntry;
import immersive_aircraft.config.configEntries.IntegerConfigEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class JsonConfig {
    public static final Logger LOGGER = LogManager.getLogger();

    public int version = 0;

    int getVersion() {
        return 1;
    }

    public JsonConfig() {
        for (Field field : Config.class.getDeclaredFields()) {
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

    public static File getConfigFile() {
        return new File("./config/" + Main.MOD_ID + ".json");
    }

    public void save() {
        try (FileWriter writer = new FileWriter(getConfigFile())) {
            version = getVersion();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Config loadOrCreate() {
        if (getConfigFile().exists()) {
            try (FileReader reader = new FileReader(getConfigFile())) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Config config = gson.fromJson(reader, Config.class);
                if (config.version != config.getVersion()) {
                    config = new Config();
                }
                config.save();
                return config;
            } catch (Exception e) {
                LOGGER.error("Failed to load Immersive Aircraft config! Default config is used for now. Delete the file to reset.");
                LOGGER.error(e);
                return new Config();
            }
        } else {
            Config config = new Config();
            config.save();
            return config;
        }
    }
}
