package immersive_aircraft.neoforge;

import immersive_aircraft.ClientMain;
import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_aircraft.item.upgrade.VehicleUpgrade;
import immersive_aircraft.item.upgrade.VehicleUpgradeRegistry;
import immersive_aircraft.neoforge.cobalt.registration.CobaltFuelRegistryImpl;
import immersive_aircraft.neoforge.cobalt.registration.RegistrationImpl.DataLoaderRegister;
import immersive_aircraft.network.s2c.AircraftDataMessage;
import immersive_aircraft.network.s2c.VehicleUpgradesMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@EventBusSubscriber(modid = Main.MOD_ID)
public class NeoForgeBusEvents {
    // Require access to the DataLoaderRegister here as forge uses events, could put this in RegistrationImpl, but it would just be messy
    public static DataLoaderRegister DATA_REGISTRY;
    public static DataLoaderRegister RESOURCE_REGISTRY;

    private static final DecimalFormat fmt = new DecimalFormat("+#;-#");
    public static boolean firstLoad = true;

    @SubscribeEvent
    public static void onClientStart(ClientTickEvent.Pre event) {
        if (firstLoad) {
            ClientMain.postLoad();
            firstLoad = false;
        }

        ClientMain.tick();
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddServerReloadListenersEvent event) {
        if (DATA_REGISTRY != null) {
            DATA_REGISTRY.getLoaders().forEach(event::addListener);
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        var level = event.getServer().overworld();
        CobaltFuelRegistryImpl.setFuelValues(level == null ? null : level.fuelValues());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        CobaltFuelRegistryImpl.setFuelValues(null);
    }

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (event.getPlayer() != null) {
            CobaltFuelRegistryImpl.setFuelValues(event.getPlayer().level().fuelValues());
        }
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CobaltFuelRegistryImpl.setFuelValues(null);
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) { // Syncing aircraft upgrades to players.
            NetworkHandler.sendToPlayer(new VehicleUpgradesMessage(), event.getPlayer());
            NetworkHandler.sendToPlayer(new AircraftDataMessage(), event.getPlayer());
        } else {
            for (ServerPlayer player : event.getPlayerList().getPlayers()) {
                NetworkHandler.sendToPlayer(new VehicleUpgradesMessage(), player);
                NetworkHandler.sendToPlayer(new AircraftDataMessage(), player);
            }
        }
    }

    @SubscribeEvent
    public static void onItemTooltips(ItemTooltipEvent event) {
        VehicleUpgrade upgrade = VehicleUpgradeRegistry.INSTANCE.getUpgrade(event.getItemStack().getItem());
        if (upgrade != null) {
            List<Component> tooltip = event.getToolTip();

            tooltip.add(Component.translatable("item.immersive_aircraft.item.upgrade").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));

            for (Map.Entry<VehicleStat, Float> entry : upgrade.getAll().entrySet()) {
                tooltip.add(Component.translatable("immersive_aircraft.upgrade." + entry.getKey().name().toLowerCase(Locale.ROOT),
                        fmt.format(entry.getValue() * 100)
                ).withStyle(entry.getValue() * (entry.getKey().positive() ? 1 : -1) > 0 ? ChatFormatting.GREEN : ChatFormatting.RED));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity().getRootVehicle() instanceof VehicleEntity) {
            event.setNewSpeed(event.getOriginalSpeed() * 5.0f);
        }
    }
}
