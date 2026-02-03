package immersive_aircraft.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public abstract class StringDrawer {
    private static final char[] regularS = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '-', '=', '(', ')'
    };
    private static final char[] superS = new char[]{
            '⁰', '¹', '²', '³', '⁴', '⁵', '⁶', '⁷', '⁸', '⁹', '⁺', '⁻', '⁼', '⁽', '⁾'
    };
    private static final char[] subS = new char[]{
            '₀', '₁', '₂', '₃', '₄', '₅', '₆', '₇', '₈', '₉', '₊', '₋', '₌', '₍', '₎'
    };

    static void drawString1(GuiGraphics context, Minecraft client, @Nullable String text, int x, int y, int color, boolean dropShadow) {
        context.drawString(client.font, text, x + 1, y + 1, color, dropShadow);
    }

    static void drawString2(GuiGraphics context, Minecraft client, @Nullable String text, int x, int y, int color, boolean dropShadow) {
        context.drawString(client.font, text, x - client.font.width(text) / 2 - 1, y + 1, color, dropShadow);
    }

    static void drawString3(GuiGraphics context, Minecraft client, @Nullable String text, int x, int y, int color, boolean dropShadow) {
        context.drawString(client.font, text, x - client.font.width(text) - 2, y + 1, color, dropShadow);
    }

    static void drawString4(GuiGraphics context, Minecraft client, @Nullable String text, int x, int y, int color, boolean dropShadow) {
        context.drawString(client.font, text, x + 1, y - client.font.lineHeight / 2 + 1, color, dropShadow);
    }

    static void drawString5(GuiGraphics context, Minecraft client, @Nullable String text, int x, int y, int color, boolean dropShadow) {
        context.drawString(client.font, text, x - client.font.width(text) / 2 - 1, y - client.font.lineHeight / 2 + 1, color, dropShadow);
    }

    static void drawString6(GuiGraphics context, Minecraft client, @Nullable String text, int x, int y, int color, boolean dropShadow) {
        context.drawString(client.font, text, x - client.font.width(text) - 2, y - client.font.lineHeight / 2 + 1, color, dropShadow);
    }

    static void drawString7(GuiGraphics context, Minecraft client, @Nullable String text, int x, int y, int color, boolean dropShadow) {
        context.drawString(client.font, text, x + 1, y - client.font.lineHeight + 1, color, dropShadow);
    }

    static void drawString8(GuiGraphics context, Minecraft client, @Nullable String text, int x, int y, int color, boolean dropShadow) {
        context.drawString(client.font, text, x - client.font.width(text) / 2 - 1, y - client.font.lineHeight + 1, color, dropShadow);
    }

    static void drawString9(GuiGraphics context, Minecraft client, @Nullable String text, int x, int y, int color, boolean dropShadow) {
        context.drawString(client.font, text, x - client.font.width(text) - 2, y - client.font.lineHeight + 1, color, dropShadow);
    }

    static String toSuperScript(String s) {
        for (int i = 0; i < 15; i++) s = s.replace(regularS[i], superS[i]);
        return s;
    }

    static String toSubScript(String s) {
        for (int i = 0; i < 15; i++) s = s.replace(regularS[i], subS[i]);
        return s;
    }
}
