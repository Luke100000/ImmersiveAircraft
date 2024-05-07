package immersive_aircraft.forge;

import immersive_aircraft.ClientMain;
import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.forge.cobalt.registration.RegistrationImpl.DataLoaderRegister;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_aircraft.item.upgrade.VehicleUpgrade;
import immersive_aircraft.item.upgrade.VehicleUpgradeRegistry;
import immersive_aircraft.network.s2c.AircraftDataMessage;
import immersive_aircraft.network.s2c.VehicleUpgradesMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@Mod.EventBusSubscriber(modid = Main.MOD_ID)
public class ForgeBusEvents {
    // Require access to the DataLoaderRegister here as forge uses events, could put this in RegistrationImpl, but it would just be messy
    public static DataLoaderRegister DATA_REGISTRY;
    public static DataLoaderRegister RESOURCE_REGISTRY;

    private static final DecimalFormat fmt = new DecimalFormat("+#;-#");
    public static boolean firstLoad = true;

    @SubscribeEvent
    public static void onClientStart(TickEvent.ClientTickEvent event) {
        //forge decided to be funny and won't trigger the client load event
        if (firstLoad) {
            ClientMain.postLoad();
            firstLoad = false;
        }

        ClientMain.tick();
    }

    @SubscribeEvent
    public static void addReloadListenerEvent(AddReloadListenerEvent event) {
        if (DATA_REGISTRY != null) {
            for (PreparableReloadListener loader : DATA_REGISTRY.getLoaders()) {
                event.addListener(loader);
            }
        }
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
