package immersive_aircraft.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import immersive_aircraft.Main;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Loads cosmetic vehicle definitions from data/&lt;namespace&gt;/vehicle_skins. */
public class VehicleSkinDataLoader extends DataLoader {
    public static final Map<ResourceLocation, VehicleSkin> REGISTRY = new HashMap<>();
    public static final Map<ResourceLocation, VehicleSkin> CLIENT_REGISTRY = new HashMap<>();
    private static final Map<ResourceLocation, VehicleSkin> DEFAULT_SKINS = new HashMap<>();
    private static final Map<ResourceLocation, VehicleSkin> CLIENT_DEFAULT_SKINS = new HashMap<>();
    private static final Comparator<VehicleSkin> DISPLAY_ORDER = Comparator
            .comparing(VehicleSkin::defaultSkin).reversed()
            .thenComparing(skin -> skin.id().toString());

    public VehicleSkinDataLoader() {
        super(new Gson(), "vehicle_skins");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller profiler) {
        REGISTRY.clear();

        jsonMap.forEach((identifier, jsonElement) -> {
            try {
                VehicleSkin skin = VehicleSkin.fromJson(identifier, jsonElement.getAsJsonObject());
                REGISTRY.put(identifier, skin);
            } catch (RuntimeException exception) {
                Main.LOGGER.error("Parsing error on vehicle skin {}: {}", identifier, exception.getMessage());
            }
        });

        CLIENT_REGISTRY.clear();
        CLIENT_REGISTRY.putAll(REGISTRY);
        rebuildDefaults(REGISTRY, DEFAULT_SKINS);
        rebuildDefaults(CLIENT_REGISTRY, CLIENT_DEFAULT_SKINS);
    }

    public static void replaceClientRegistry(Map<ResourceLocation, VehicleSkin> skins) {
        CLIENT_REGISTRY.clear();
        CLIENT_REGISTRY.putAll(skins);
        rebuildDefaults(CLIENT_REGISTRY, CLIENT_DEFAULT_SKINS);
    }

    private static void rebuildDefaults(Map<ResourceLocation, VehicleSkin> skins, Map<ResourceLocation, VehicleSkin> defaults) {
        defaults.clear();
        skins.values().stream()
                .filter(VehicleSkin::defaultSkin)
                .sorted(DISPLAY_ORDER)
                .forEach(skin -> defaults.putIfAbsent(skin.vehicle(), skin));
    }

    public static List<ResourceLocation> getAvailableSkinIds(Player player, ResourceLocation vehicle) {
        return REGISTRY.values().stream()
                .filter(skin -> skin.vehicle().equals(vehicle))
                .filter(skin -> skin.isUnlockedFor(player))
                .sorted(DISPLAY_ORDER)
                .map(VehicleSkin::id)
                .toList();
    }

    public static boolean canSelect(Player player, ResourceLocation vehicle, ResourceLocation skinId) {
        VehicleSkin skin = REGISTRY.get(skinId);
        return skin != null && skin.vehicle().equals(vehicle) && skin.isUnlockedFor(player);
    }

    public static Optional<VehicleSkin> getClientSkin(ResourceLocation vehicle, ResourceLocation selectedSkin) {
        return getSkin(CLIENT_REGISTRY, CLIENT_DEFAULT_SKINS, vehicle, selectedSkin);
    }

    public static Optional<VehicleSkin> getServerSkin(ResourceLocation vehicle, ResourceLocation selectedSkin) {
        return getSkin(REGISTRY, DEFAULT_SKINS, vehicle, selectedSkin);
    }

    private static Optional<VehicleSkin> getSkin(Map<ResourceLocation, VehicleSkin> skins,
                                                 Map<ResourceLocation, VehicleSkin> defaults,
                                                 ResourceLocation vehicle, ResourceLocation selectedSkin) {
        VehicleSkin selected = selectedSkin == null ? null : skins.get(selectedSkin);
        if (selected != null && selected.vehicle().equals(vehicle)) {
            return Optional.of(selected);
        }

        return Optional.ofNullable(defaults.get(vehicle));
    }

    public static Optional<ResourceLocation> getActiveServerSkinId(ResourceLocation vehicle, ResourceLocation selectedSkin) {
        VehicleSkin selected = selectedSkin == null ? null : REGISTRY.get(selectedSkin);
        if (selected != null && selected.vehicle().equals(vehicle)) {
            return Optional.of(selected.id());
        }

        return Optional.ofNullable(DEFAULT_SKINS.get(vehicle)).map(VehicleSkin::id);
    }

    public static Optional<VehicleSkin> getClient(ResourceLocation skinId) {
        return Optional.ofNullable(CLIENT_REGISTRY.get(skinId));
    }
}
