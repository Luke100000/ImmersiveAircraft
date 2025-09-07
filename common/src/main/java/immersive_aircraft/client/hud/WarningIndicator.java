package immersive_aircraft.client.hud;

import immersive_aircraft.client.OverlayRenderer;
import immersive_aircraft.entity.EngineVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.NoteBlock;

import java.util.EnumMap;

public class WarningIndicator implements Indicator {
    public static final WarningIndicator INSTANCE = new WarningIndicator();
    private boolean miniHUD = false;
    private boolean cWarning = false;
    private boolean cMsl = false;
    public EnumMap<EngineVehicle.Cautions, Boolean> cMap = new EnumMap<>(EngineVehicle.Cautions.class);
    private static final int colorBG = FastColor.ARGB32.color(255, 215, 215, 215);
    private static final int colorFG = FastColor.ARGB32.color(255, 31, 31, 31);
    private static final int colorLt0 = FastColor.ARGB32.color(255, 127, 127, 127);
    private static final int colorLt1 = FastColor.ARGB32.color(255, 255, 0, 0);
    private static final int colorLt2 = FastColor.ARGB32.color(255, 255, 191, 0);
    private static final int colorLt3 = FastColor.ARGB32.color(255, 191, 191, 191);

    public WarningIndicator() {
        for (EngineVehicle.Cautions c : EngineVehicle.Cautions.values()) cMap.compute(c, (cautions, v) -> false);
    }
    @Override
    public void update(Minecraft client, EngineVehicle aircraft) {
        if (!aircraft.level().isClientSide || client.isPaused()) return;
        if (aircraft.mslWarning > 0) {
            if (OverlayRenderer.INSTANCE.tk % 10 == 0)
                cMsl = !cMsl;
            if(OverlayRenderer.INSTANCE.tk % 5 == 0) {
                aircraft.level().playLocalSound(aircraft.getX(), aircraft.getY() + aircraft.getBbHeight() * 0.5, aircraft.getZ(),
                        SoundEvents.NOTE_BLOCK_PLING.value(),
                        aircraft.getSoundSource(), 1.0f, NoteBlock.getPitchFromNote(24), false);
                aircraft.level().playLocalSound(aircraft.getX(), aircraft.getY() + aircraft.getBbHeight() * 0.5, aircraft.getZ(),
                        SoundEvents.NOTE_BLOCK_PLING.value(),
                        aircraft.getSoundSource(), 1.0f, NoteBlock.getPitchFromNote(22), false);
            }
        } else cMsl = false;
        if (aircraft.mainWarning > 0) {
            if (OverlayRenderer.INSTANCE.tk % 10 == 0) {
                cWarning = !cWarning;
                if (!cMsl) aircraft.level().playLocalSound(aircraft.getX(), aircraft.getY() + aircraft.getBbHeight() * 0.5, aircraft.getZ(),
                        SoundEvents.NOTE_BLOCK_BIT.value(),
                        aircraft.getSoundSource(), 1.0f, NoteBlock.getPitchFromNote(cWarning ? 16 : 24), false);
            }
        } else cWarning = false;
        for (EngineVehicle.Cautions c : EngineVehicle.Cautions.values()) {
            if (aircraft.cautions.get(c) > 0) {
                if (OverlayRenderer.INSTANCE.tk % 15 == 0)
                    cMap.compute(c, (cautions, v) -> Boolean.FALSE.equals(v));
                if (aircraft.mainWarning == 0 && aircraft.mslWarning == 0 && OverlayRenderer.INSTANCE.tk % 60 == 0)
                    aircraft.level().playLocalSound(aircraft.getX(), aircraft.getY() + aircraft.getBbHeight() * 0.5, aircraft.getZ(),
                            SoundEvents.NOTE_BLOCK_BIT.value(),
                            aircraft.getSoundSource(), 1.0f, NoteBlock.getPitchFromNote(5), false);
            } else cMap.put(c, false);
        }
    }

    public void drawDashboard(GuiGraphics context, Minecraft client, int baseX, int baseY, EngineVehicle aircraft, int color) {
        miniHUD = true;
        drawHUD(context, client, baseX, baseY - 18, 100, aircraft, color, null);
        miniHUD = false;
    }

    public void drawHUD(GuiGraphics context, Minecraft client, int baseX, int baseY, int width, EngineVehicle aircraft, int color, int[] edge) {
        if (cMsl) {
            if (edgeCheck(edge, client.font.width("[MISSILE]") / 4, client.font.lineHeight / 2, baseX + 1, baseY))
                StringDrawer.drawString8(context, client, "[MISSILE]", baseX + 1, baseY, color, miniHUD);
        } else if (cWarning) {
            if (edgeCheck(edge, client.font.width("[WARNING]") / 4, client.font.lineHeight / 2, baseX + 1, baseY))
                StringDrawer.drawString8(context, client, "[WARNING]", baseX + 1, baseY, color, miniHUD);
        }
        if (!cMap.containsValue(true)) return;
        StringBuilder builder = new StringBuilder();
        cMap.forEach((caution, v) -> {
            if (v) builder.append('[').append(caution.name().toUpperCase().replace('_', ' ')).append(']');
        });
        String s = builder.toString();
        if (edgeCheck(edge, client.font.width(s) / 4, client.font.lineHeight / 2, baseX + 1, baseY))
            StringDrawer.drawString2(context, client, s, baseX + 1, baseY, color, miniHUD);
    }

    @Override
    public void drawDials(GuiGraphics context, Minecraft client, int baseX, int baseY, int scale, EngineVehicle aircraft) {
        // dial 29x75
        context.fill(baseX - 14, baseY - 37, baseX + 14 + 1, baseY + 37 + 1, colorBG);
        // border
        context.fill(baseX - 14, baseY - 37, baseX + 14 + 1, baseY - 35, colorFG);
        context.fill(baseX - 14, baseY - 37, baseX - 12, baseY + 37 + 1, colorFG);
        context.fill(baseX - 14, baseY + 35 + 1, baseX + 14 + 1, baseY + 37 + 1, colorFG);
        context.fill(baseX + 12 + 1, baseY - 37, baseX + 14 + 1, baseY + 37 + 1, colorFG);
        OverlayRenderer.drawScrew(context, baseX, baseY - 32, 1, true, colorFG);
        OverlayRenderer.drawScrew(context, baseX, baseY + 32, 1, false, colorFG);
        // caution lamp
        context.fill(baseX - 11, baseY - 26, baseX + 11 + 1, baseY - 6 + 1, colorFG);
        context.fill(baseX - 10, baseY - 25, baseX + 10 + 1, baseY - 7 + 1, cWarning ? colorLt1 : colorLt0);
        StringDrawer.drawString5(context, client, "MAIN", baseX + 2, baseY - 16, colorLt3, false);
        context.fill(baseX - 11, baseY - 2, baseX + 11 + 1, baseY + 10 + 1, colorFG);
        context.fill(baseX - 10, baseY - 1, baseX + 10 + 1, baseY + 9 + 1, cMap.get(EngineVehicle.Cautions.VOID) ? colorLt2 : colorLt0);
        StringDrawer.drawString5(context, client, "VOID", baseX + 2, baseY + 4, colorLt3, false);
        context.fill(baseX - 11, baseY + 14, baseX + 11 + 1, baseY + 26 + 1, colorFG);
        context.fill(baseX - 10, baseY + 15, baseX + 10 + 1, baseY + 25 + 1, cMap.get(EngineVehicle.Cautions.DAMAGED) ? colorLt2 : colorLt0);
        StringDrawer.drawString5(context, client, "DMG", baseX + 2, baseY + 20, colorLt3, false);
    }
}
