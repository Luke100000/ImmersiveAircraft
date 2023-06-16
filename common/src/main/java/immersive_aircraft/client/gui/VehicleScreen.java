package immersive_aircraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import immersive_aircraft.Main;
import immersive_aircraft.entity.EngineAircraft;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.screen.VehicleScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class VehicleScreen extends HandledScreen<VehicleScreenHandler> {
    private static final Identifier TEXTURE = Main.locate("textures/gui/container/inventory.png");

    public static int titleHeight = 10;
    public static int baseHeight = 86;
    public static int containerSize;

    public VehicleScreen(VehicleScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        containerSize = handler.getVehicle().getInventoryDescription().getHeight();

        backgroundHeight = baseHeight + containerSize + titleHeight * 2;
        playerInventoryTitleY = containerSize + titleHeight;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        //nop
    }

    protected void drawRectangle(DrawContext context, int x, int y, int h, int w) {
        //corners
        context.drawTexture(TEXTURE, x, y, 176, 0, 16, 16, 512, 256);
        context.drawTexture(TEXTURE, x + w - 16, y, 176 + 32, 0, 16, 16, 512, 256);
        context.drawTexture(TEXTURE, x + w - 16, y + h - 16, 176 + 32, 32, 16, 16, 512, 256);
        context.drawTexture(TEXTURE, x, y + h - 16, 176, 32, 16, 16, 512, 256);

        //edges
        context.drawTexture(TEXTURE, x + 16, y, w - 32, 16, 176 + 16, 0, 16, 16, 512, 256);
        context.drawTexture(TEXTURE, x + 16, y + h - 16, w - 32, 16, 176 + 16, 32, 16, 16, 512, 256);
        context.drawTexture(TEXTURE, x, y + 16, 16, h - 32, 176, 16, 16, 16, 512, 256);
        context.drawTexture(TEXTURE, x + w - 16, y + 16, 16, h - 32, 176 + 32, 16, 16, 16, 512, 256);

        //center
        context.drawTexture(TEXTURE, x + 16, y + 16, w - 32, h - 32, 176 + 16, 16, 16, 16, 512, 256);
    }

    protected void drawCustomBackground(DrawContext context) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, containerSize + titleHeight * 2, 512, 256);
        context.drawTexture(TEXTURE, x, y + containerSize + titleHeight * 2 - 4, 0, 222 - baseHeight, backgroundWidth, baseHeight, 512, 256);

        for (VehicleInventoryDescription.Rectangle rectangle : handler.getVehicle().getInventoryDescription().getRectangles()) {
            drawRectangle(context, x + rectangle.x(), y + rectangle.y(), rectangle.w(), rectangle.h());
        }
    }

    private void drawImage(DrawContext context, int x, int y, int u, int v, int w, int h) {
        context.drawTexture(TEXTURE, x, y, u, v, w, h, 512, 256);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawCustomBackground(context);

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int titleHeight = 10;

        for (VehicleInventoryDescription.Slot slot : handler.getVehicle().getInventoryDescription().getSlots()) {
            switch (slot.type) {
                case INVENTORY -> drawImage(context, x + slot.x - 1, y + titleHeight + slot.y - 1, 284, 0, 18, 18);
                case BOILER -> {
                    drawImage(context, x + slot.x - 4, y + titleHeight + slot.y - 18, 318, 0, 24, 39);
                    if (handler.getVehicle() instanceof EngineAircraft engineAircraft && engineAircraft.getFuelUtilization() > 0.0) {
                        drawImage(context, x + slot.x - 4, y + titleHeight + slot.y - 18, 318 + 30, 0, 24, 39);
                    }
                }
                default -> {
                    if (handler.getVehicle().getInventory().getStack(slot.index).isEmpty()) {
                        switch (slot.type) {
                            case WEAPON ->
                                    drawImage(context, x + slot.x - 3, y + titleHeight + slot.y - 3, 262, 22, 22, 22);
                            case UPGRADE ->
                                    drawImage(context, x + slot.x - 3, y + titleHeight + slot.y - 3, 262, 22 * 2, 22, 22);
                            case BANNER ->
                                    drawImage(context, x + slot.x - 3, y + titleHeight + slot.y - 3, 262, 22 * 3, 22, 22);
                            case DYE ->
                                    drawImage(context, x + slot.x - 3, y + titleHeight + slot.y - 3, 262, 22 * 4, 22, 22);
                            case BOOSTER ->
                                    drawImage(context, x + slot.x - 3, y + titleHeight + slot.y - 3, 262, 22 * 5, 22, 22);
                        }
                    } else {
                        drawImage(context, x + slot.x - 3, y + titleHeight + slot.y - 3, 262, 0, 22, 22);
                    }
                }
            }
        }

        super.render(context, mouseX, mouseY, delta);

        // Slot tooltip
        if (focusedSlot != null && !focusedSlot.hasStack() && focusedSlot.inventory == handler.getVehicle().getInventory()) {
            VehicleInventoryDescription.Slot slot = handler.getVehicle().getInventoryDescription().getSlots().get(focusedSlot.getIndex());
            if (slot.type == VehicleInventoryDescription.SlotType.DYE || slot.type == VehicleInventoryDescription.SlotType.BOOSTER || slot.type == VehicleInventoryDescription.SlotType.BOILER || slot.type == VehicleInventoryDescription.SlotType.UPGRADE || slot.type == VehicleInventoryDescription.SlotType.BANNER || slot.type == VehicleInventoryDescription.SlotType.WEAPON) {
                context.drawTooltip(this.textRenderer, List.of(Text.translatable("immersive_aircraft.slot." + slot.type.name().toLowerCase(Locale.ROOT))), Optional.empty(), mouseX, mouseY);
            }
        } else {
            drawMouseoverTooltip(context, mouseX, mouseY);
        }
    }

    @Override
    protected void init() {
        super.init();

        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        if (super.isClickOutsideBounds(mouseX, mouseY, left, top, button)) {
            for (VehicleInventoryDescription.Rectangle rectangle : handler.getVehicle().getInventoryDescription().getRectangles()) {
                if (mouseX > rectangle.x() + x && mouseX < rectangle.x() + rectangle.w() + x && mouseY > rectangle.y() + y && mouseY < rectangle.y() + rectangle.h() + y) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
