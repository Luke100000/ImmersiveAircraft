package immersive_aircraft.config;

import immersive_aircraft.Main;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AutoEnterRules {
    private AutoEnterRules() {
    }

    public static boolean canAutoEnter(EntityType<?> type) {
        Boolean exactRule = Rules.EXACT.get(type);
        if (exactRule != null) {
            return exactRule;
        }

        boolean matchedAllow = false;
        for (TagRule rule : Rules.TAGS) {
            if (type.is(rule.tag())) {
                if (!rule.allowed()) {
                    return false;
                }
                matchedAllow = true;
            }
        }

        return matchedAllow || Config.getInstance().defaultCanEnter;
    }

    private static void warnInvalidRule(String key) {
        Main.LOGGER.warn("Ignoring invalid canEnter rule '{}'. Expected an entity ID or an entity-type tag prefixed with '#'.", key);
    }

    private static final class Rules {
        private static final Map<EntityType<?>, Boolean> EXACT = new HashMap<>();
        private static final List<TagRule> TAGS = new ArrayList<>();

        static {
            Map<String, Boolean> configuredRules = Config.getInstance().canEnter;
            if (configuredRules == null) {
                Main.LOGGER.warn("Ignoring null canEnter config; no entity boarding overrides will be applied.");
            } else {
                configuredRules.forEach(Rules::parse);
            }
        }

        private static void parse(String key, Boolean allowed) {
            if (key == null || allowed == null) {
                warnInvalidRule(key);
                return;
            }

            boolean tagRule = key.startsWith("#");
            ResourceLocation id = ResourceLocation.tryParse(tagRule ? key.substring(1) : key);
            if (id == null) {
                warnInvalidRule(key);
                return;
            }

            if (tagRule) {
                TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, id);
                TAGS.add(new TagRule(tag, allowed));
                return;
            }

            BuiltInRegistries.ENTITY_TYPE.getOptional(id).ifPresentOrElse(
                    type -> EXACT.put(type, allowed),
                    () -> warnInvalidRule(key)
            );
        }
    }

    private record TagRule(TagKey<EntityType<?>> tag, boolean allowed) {
    }
}
