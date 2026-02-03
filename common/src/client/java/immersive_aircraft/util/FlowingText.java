package immersive_aircraft.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FlowingText {
    public static List<Component> wrap(Component text, int maxWidth) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.isSameThread()) {
            return client.font.getSplitter().splitLines(text, maxWidth, Style.EMPTY).stream().map(line -> {
                MutableComponent compiled = Component.literal("");
                line.visit((s, t) -> {
                    compiled.append(Component.translatable(t).setStyle(s));
                    return Optional.empty();
                }, text.getStyle());
                return compiled;
            }).collect(Collectors.toList());
        } else {
            return List.of(text);
        }
    }
}
