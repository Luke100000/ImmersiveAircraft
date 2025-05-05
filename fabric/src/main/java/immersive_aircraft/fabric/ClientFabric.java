package immersive_aircraft.fabric;

import immersive_aircraft.ClientMain;
import immersive_aircraft.ItemColors;
import immersive_aircraft.Renderer;
import immersive_aircraft.WeaponRendererRegistry;
import immersive_aircraft.client.KeyBindings;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_aircraft.item.upgrade.VehicleUpgrade;
import immersive_aircraft.item.upgrade.VehicleUpgradeRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ClientFabric implements ClientModInitializer {
    private final DecimalFormat fmt = new DecimalFormat("+#;-#");

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(event -> ClientMain.postLoad());

        ClientTickEvents.START_CLIENT_TICK.register(event -> ClientMain.tick());

        Renderer.bootstrap();
        WeaponRendererRegistry.bootstrap();

        ItemColors.ITEM_COLORS.forEach((item, itemColor) -> ColorProviderRegistry.ITEM.register(itemColor, item));
        ItemColors.ITEM_COLOR_PROVIDERS.forEach((item, itemColor) -> ColorProviderRegistry.ITEM.register(itemColor, item.get()));

        KeyBindings.list.forEach(KeyBindingHelper::registerKeyBinding);
        ItemTooltipCallback.EVENT.register(this::itemTooltipCallback); // For aircraft upgrade tooltips
    }

    /**
     * Handles adding ToolTips to aircraft upgrades.
     */
    private void itemTooltipCallback(ItemStack stack, TooltipFlag context, List<Component> tooltip) {
        VehicleUpgrade upgrade = VehicleUpgradeRegistry.INSTANCE.getUpgrade(stack.getItem());
        if (upgrade != null) {
            tooltip.add(Component.translatable("item.immersive_aircraft.item.upgrade").withStyle(ChatFormatting.GRAY));

            for (Map.Entry<VehicleStat, Float> entry : upgrade.getAll().entrySet()) {
                tooltip.add(Component.translatable("immersive_aircraft.upgrade." + entry.getKey().name().toLowerCase(Locale.ROOT),
                        fmt.format(entry.getValue() * 100)
                ).withStyle(entry.getValue() * (entry.getKey().positive() ? 1 : -1) > 0 ? ChatFormatting.GREEN : ChatFormatting.RED));
            }
        }
    }
}
