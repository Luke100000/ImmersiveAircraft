package immersive_aircraft.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FlowingText {
    public static List<Text> wrap(Text text, int maxWidth) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.isOnThread()) {
            return client.textRenderer.getTextHandler().wrapLines(text, maxWidth, Style.EMPTY).stream().map(line -> {
                LiteralText compiled = new LiteralText("");
                line.visit((s, t) -> {
                    compiled.append(new LiteralText(t).setStyle(s));
                    return Optional.empty();
                }, text.getStyle());
                return compiled;
            }).collect(Collectors.toList());
        } else {
            return List.of(text);
        }
    }
}
