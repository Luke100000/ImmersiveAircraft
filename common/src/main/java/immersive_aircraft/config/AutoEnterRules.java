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
    private static final Map<EntityType<?>, Boolean> EXACT_RULES = new HashMap<>();
    private static final List<TagRule> TAG_RULES = new ArrayList<>();

    static {
        Map<String, Boolean> configuredRules = Config.getInstance().canEnter;
        if (configuredRules == null) {
            Main.LOGGER.warn("Ignoring null canEnter config; no entity boarding overrides will be applied.");
        } else {
            configuredRules.forEach(AutoEnterRules::parse);
        }
    }

    private AutoEnterRules() {
    }

    public static boolean canAutoEnter(EntityType<?> type) {
        Boolean exactRule = EXACT_RULES.get(type);
        if (exactRule != null) {
            return exactRule;
        }

        boolean matchedAllow = false;
        for (TagRule rule : TAG_RULES) {
            if (type.is(rule.tag())) {
                if (!rule.allowed()) {
                    return false;
                }
                matchedAllow = true;
            }
        }

        return matchedAllow || Config.getInstance().defaultCanEnter;
    }

    private static void parse(String key, Boolean allowed) {
        if (allowed == null) {
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
            TAG_RULES.add(new TagRule(TagKey.create(Registries.ENTITY_TYPE, id), allowed));
            return;
        }

        BuiltInRegistries.ENTITY_TYPE.getOptional(id).ifPresentOrElse(
                type -> EXACT_RULES.put(type, allowed),
                () -> warnInvalidRule(key)
        );
    }

    private static void warnInvalidRule(String key) {
        Main.LOGGER.warn("Ignoring invalid canEnter rule '{}'. Expected an entity ID or an entity-type tag prefixed with '#'.", key);
    }

    private record TagRule(TagKey<EntityType<?>> tag, boolean allowed) {
    }
}
