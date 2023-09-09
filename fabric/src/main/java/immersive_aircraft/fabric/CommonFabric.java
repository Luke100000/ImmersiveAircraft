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
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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

        CreativeModeTab group = FabricItemGroup.builder()
                .title(ItemGroups.getDisplayName())
                .icon(ItemGroups::getIcon)
                .entries((enabledFeatures, entries) -> entries.addAll(Items.getSortedItems()))
                .build();

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Main.locate("group"), group);

        // Register event for syncing aircraft upgrades.
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(this::onSyncDatapack);
        ItemTooltipCallback.EVENT.register(this::itemTooltipCallback); // For aircraft upgrade tooltips
    }

    /**
     * Handles adding ToolTips to aircraft upgrades.
     */
    private void itemTooltipCallback(ItemStack stack, TooltipFlag context, List<Component> tooltip) {
        AircraftUpgrade upgrade = AircraftUpgradeRegistry.INSTANCE.getUpgrade(stack.getItem());
        if(upgrade != null) {
            tooltip.add(Component.translatable("item.immersive_aircraft.item.upgrade").withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));

            for (Map.Entry<AircraftStat, Float> entry : upgrade.getAll().entrySet()) {
                tooltip.add(Component.translatable("immersive_aircraft.upgrade." + entry.getKey().name().toLowerCase(Locale.ROOT),
                        fmt.format(entry.getValue() * 100)
                ).formatted(entry.getValue() * (entry.getKey().isPositive() ? 1 : -1) > 0 ? ChatFormatting.GREEN : ChatFormatting.RED));
            }
        }
    }

    /**
     * Send sync packets for upgrades when datapack is reloaded.
     */
    private void onSyncDatapack(ServerPlayer player,  boolean joined) {
        NetworkHandler.sendToPlayer(new AircraftUpgradesMessage(), player);
        NetworkHandler.sendToPlayer(new AircraftBaseUpgradesMessage(), player);
    }

}

