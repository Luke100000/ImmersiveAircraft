package immersive_aircraft.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.Collections;
import java.util.List;

public class FlowingText {
    public static List<String> wrap(String text, int maxWidth) {
        Minecraft client = Minecraft.getMinecraft();
        if (client.world != null) {
            FontRenderer fontRenderer = client.fontRenderer;
            return fontRenderer.listFormattedStringToWidth(text, maxWidth);
        } else {
            return Collections.singletonList(text);
        }
    }
}
