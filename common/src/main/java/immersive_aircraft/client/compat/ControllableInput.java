package immersive_aircraft.client.compat;

import com.mrcrayfish.controllable.Controllable;
import com.mrcrayfish.controllable.client.Action;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.binding.ButtonBindings;
import com.mrcrayfish.controllable.client.binding.KeyAdapterBinding;
import com.mrcrayfish.controllable.client.binding.context.InGameContext;
import com.mrcrayfish.controllable.client.binding.handlers.OnPressHandler;
import com.mrcrayfish.controllable.client.input.Buttons;
import com.mrcrayfish.controllable.client.input.Controller;
import com.mrcrayfish.controllable.platform.ClientServices;
import immersive_aircraft.client.ControllerInput;
import immersive_aircraft.client.KeyBindings;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class ControllableInput implements ControllerInput {
    public ControllableInput() {
        Controllable.getBindingRegistry().addKeyAdapter(new KeyAdapterBinding(Buttons.B, KeyBindings.dismount));
    }

    public static void registerDefaults() {
        InGameContext context = new InGameContext(ResourceLocation.fromNamespaceAndPath("immersive_aircraft", "boost")) {
            @Override
            public int priority() {
                // Boost handles L3 before Sprint; its handler falls through outside aircraft.
                return 1;
            }
        };
        Controllable.getBindingRegistry().register(new ButtonBinding(Buttons.LEFT_THUMB_STICK,
                KeyBindings.boost.getName(), KeyBindings.boost.getCategory(), context, OnPressHandler.create(input -> {
                    if (input.player().filter(player -> player.getRootVehicle() instanceof VehicleEntity).isEmpty()
                            || Controllable.getBindingRegistry().getKeyAdapterByDescriptionKey(KeyBindings.boost.getName() + ".custom") != null) {
                        return Optional.empty();
                    }
                    return Optional.of(() -> ClientServices.CLIENT.setKeyPressTime(KeyBindings.boost, 1));
                })));
    }

    @Override
    public float strength(KeyMapping key) {
        Controller controller = Controllable.getController();
        if (controller == null || !controller.isOpen() || !controller.isAccessible()) {
            return -1;
        }
        ButtonBinding binding = getBinding(key);
        if (binding == null || binding.isUnbound()) return -1;
        if (!binding.getContext().isActive() || Controllable.getRadialMenu().isVisible()) return 0;
        return switch (binding.getButton()) {
            case Buttons.LEFT_TRIGGER -> controller.getLTriggerValue();
            case Buttons.RIGHT_TRIGGER -> controller.getRTriggerValue();
            default -> controller.getPressedValue(binding.getButton());
        };
    }

    private static ButtonBinding getBinding(KeyMapping key) {
        var registry = Controllable.getBindingRegistry();
        ButtonBinding binding = registry.getKeyAdapterByDescriptionKey(key.getName() + ".custom");
        return binding != null && (!binding.isUnbound() || key == KeyBindings.boost)
                ? binding : registry.getBindingByDescriptionKey(key.getName());
    }

    public static void addHints(Map<ButtonBinding, Action> actions) {
        Minecraft client = Minecraft.getInstance();
        if (client.screen != null || client.player == null || Controllable.getRadialMenu().isVisible()
                || !(client.player.getRootVehicle() instanceof VehicleEntity)) return;

        List.of(ButtonBindings.JUMP, ButtonBindings.SNEAK, ButtonBindings.SPRINT,
                ButtonBindings.ATTACK, ButtonBindings.USE_ITEM).forEach(actions.keySet()::remove);
        for (int order = 0; order < AircraftGuide.HINT_ORDER.size(); order++) {
            KeyMapping key = AircraftGuide.HINT_ORDER.get(order);
            String label = AircraftGuide.label(key, client.player);
            if (label == null) continue;
            ButtonBinding binding = getBinding(AircraftGuide.binding(key, client.player));
            KeyMapping fallback = KeyBindings.getFallbackKey(key);
            if ((binding == null || binding.isUnbound()) && fallback != null) {
                binding = getBinding(fallback);
            }
            if (binding != null && !binding.isUnbound() && binding.getContext().isActive()) {
                actions.put(binding, new AircraftAction(Component.translatable(label),
                        key == KeyBindings.dismount || key == KeyBindings.use ? Action.Side.RIGHT : Action.Side.LEFT, order));
            }
        }
    }

    public static int compareHints(Action first, Action second) {
        int side = first.getSide().compareTo(second.getSide());
        if (side != 0) return side;
        int order = Integer.compare(first instanceof AircraftAction aircraft ? aircraft.order : Integer.MAX_VALUE,
                second instanceof AircraftAction aircraft ? aircraft.order : Integer.MAX_VALUE);
        return order != 0 ? order : first.compareTo(second);
    }

    public static Set<Integer> orderedButtons(Map<Integer, Action> actions) {
        if (actions.values().stream().noneMatch(action -> action instanceof AircraftAction)) return actions.keySet();
        return actions.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(ControllableInput::compareHints))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static final class AircraftAction extends Action {
        private final int order;

        private AircraftAction(Component label, Side side, int order) {
            super(label, side);
            this.order = order;
        }
    }
}
