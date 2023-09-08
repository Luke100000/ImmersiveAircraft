package immersive_aircraft.fabric;

import immersive_aircraft.*;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.fabric.cobalt.network.NetworkHandlerImpl;
import immersive_aircraft.fabric.cobalt.registration.RegistrationImpl;
import immersive_aircraft.item.upgrade.AircraftStat;
import immersive_aircraft.item.upgrade.AircraftUpgrade;
import immersive_aircraft.item.upgrade.AircraftUpgradeRegistry;
import immersive_aircraft.network.s2c.AircraftBaseUpgradesMessage;
import immersive_aircraft.network.s2c.AircraftUpgradesMessage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CommonFabric implements ModInitializer {

    private final DecimalFormat fmt = new DecimalFormat("+#;-#");

    @Override
    public void onInitialize() {
        new RegistrationImpl();
        new NetworkHandlerImpl();

        Items.bootstrap();
        Sounds.bootstrap();
        Entities.bootstrap();
        DataLoaders.register();
        Messages.loadMessages();

        // Register event for syncing aircraft upgrades.
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(this::onSyncDatapack);
        ItemTooltipCallback.EVENT.register(this::itemTooltipCallback); // For aircraft upgrade tooltips
    }

    /**
     * Handles adding ToolTips to aircraft upgrades.
     */
    private void itemTooltipCallback(ItemStack stack, TooltipContext context, List<Text> tooltip) {
        AircraftUpgrade upgrade = AircraftUpgradeRegistry.INSTANCE.getUpgrade(stack.getItem());
        if (upgrade != null) {
            tooltip.add(Text.translatable("item.immersive_aircraft.item.upgrade").formatted(Formatting.GRAY).formatted(Formatting.ITALIC));

            for (Map.Entry<AircraftStat, Float> entry : upgrade.getAll().entrySet()) {
                tooltip.add(Text.translatable("immersive_aircraft.upgrade." + entry.getKey().name().toLowerCase(Locale.ROOT),
                        fmt.format(entry.getValue() * 100)
                ).formatted(entry.getValue() * (entry.getKey().isPositive() ? 1 : -1) > 0 ? Formatting.GREEN : Formatting.RED));
            }
        }
    }

    /**
     * Send sync packets for upgrades when datapack is reloaded.
     */
    private void onSyncDatapack(ServerPlayerEntity player, boolean joined) {
        NetworkHandler.sendToPlayer(new AircraftUpgradesMessage(), player);
        NetworkHandler.sendToPlayer(new AircraftBaseUpgradesMessage(), player);
    }

}

