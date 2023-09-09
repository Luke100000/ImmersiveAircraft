package immersive_aircraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import immersive_aircraft.Main;
import immersive_aircraft.entity.EngineAircraft;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.screen.VehicleScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class VehicleScreen extends HandledScreen<VehicleScreenHandler> {
    private static final Identifier TEXTURE = Main.locate("textures/gui/container/inventory.png");

    public static final int TITLE_HEIGHT = 10;
    public static final int BASE_HEIGHT = 86;

    public int containerSize;

    public VehicleScreen(VehicleScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        containerSize = handler.getVehicle().getInventoryDescription().getHeight();

        backgroundHeight = BASE_HEIGHT + containerSize + TITLE_HEIGHT * 2;
        playerInventoryTitleY = containerSize + TITLE_HEIGHT;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        // nop
    }

    protected void drawRectangle(MatrixStack matrices, int x, int y, int h, int w) {
        //corners
        drawTexture(matrices, x, y, 176, 0, 16, 16, 512, 256);
        drawTexture(matrices, x + w - 16, y, 176 + 32, 0, 16, 16, 512, 256);
        drawTexture(matrices, x + w - 16, y + h - 16, 176 + 32, 32, 16, 16, 512, 256);
        drawTexture(matrices, x, y + h - 16, 176, 32, 16, 16, 512, 256);

        //edges
        drawTexture(matrices, x + 16, y, w - 32, 16, 176 + 16, 0, 16, 16, 512, 256);
        drawTexture(matrices, x + 16, y + h - 16, w - 32, 16, 176 + 16, 32, 16, 16, 512, 256);
        drawTexture(matrices, x, y + 16, 16, h - 32, 176, 16, 16, 16, 512, 256);
        drawTexture(matrices, x + w - 16, y + 16, 16, h - 32, 176 + 32, 16, 16, 16, 512, 256);

        //center
        drawTexture(matrices, x + 16, y + 16, w - 32, h - 32, 176 + 16, 16, 16, 16, 512, 256);
    }

    protected void drawCustomBackground(MatrixStack matrices) {
        this.renderBackground(matrices);

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        drawTexture(matrices, x, y, 0, 0, backgroundWidth, containerSize + TITLE_HEIGHT * 2, 512, 256);
        drawTexture(matrices, x, y + containerSize + TITLE_HEIGHT * 2 - 4, 0, 222 - BASE_HEIGHT, backgroundWidth, BASE_HEIGHT, 512, 256);

        for (Rect2i rectangle : handler.getVehicle().getInventoryDescription().getRectangles()) {
            drawRectangle(matrices, x + rectangle.getX(), y + rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
        }
    }

    private void drawImage(MatrixStack matrices, int x, int y, int u, int v, int w, int h) {
        drawTexture(matrices, x, y, u, v, w, h, 512, 256);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        drawCustomBackground(matrices);

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int titleHeight = 10;

        for (VehicleInventoryDescription.Slot slot : handler.getVehicle().getInventoryDescription().getSlots()) {
            switch (slot.type) {
                case INVENTORY -> drawImage(matrices, x + slot.x - 1, y + titleHeight + slot.y - 1, 284, 0, 18, 18);
                case BOILER -> {
                    drawImage(matrices, x + slot.x - 4, y + titleHeight + slot.y - 18, 318, 0, 24, 39);
                    if (handler.getVehicle() instanceof EngineAircraft engineAircraft && engineAircraft.getFuelUtilization() > 0.0) {
                        drawImage(matrices, x + slot.x - 4, y + titleHeight + slot.y - 18, 318 + 30, 0, 24, 39);
                    }
                }
                default -> {
                    if (handler.getVehicle().getInventory().getStack(slot.index).isEmpty()) {
                        switch (slot.type) {
                            case WEAPON ->
                                    drawImage(matrices, x + slot.x - 3, y + titleHeight + slot.y - 3, 262, 22, 22, 22);
                            case UPGRADE ->
                                    drawImage(matrices, x + slot.x - 3, y + titleHeight + slot.y - 3, 262, 22 * 2, 22, 22);
                            case BANNER ->
                                    drawImage(matrices, x + slot.x - 3, y + titleHeight + slot.y - 3, 262, 22 * 3, 22, 22);
                            case DYE ->
                                    drawImage(matrices, x + slot.x - 3, y + titleHeight + slot.y - 3, 262, 22 * 4, 22, 22);
                            case BOOSTER ->
                                    drawImage(matrices, x + slot.x - 3, y + titleHeight + slot.y - 3, 262, 22 * 5, 22, 22);
                        }
                    } else {
                        drawImage(matrices, x + slot.x - 3, y + titleHeight + slot.y - 3, 262, 0, 22, 22);
                    }
                }
            }
        }

        super.render(matrices, mouseX, mouseY, delta);

        // Slot tooltip
        if (focusedSlot != null && !focusedSlot.hasStack() && focusedSlot.inventory == handler.getVehicle().getInventory()) {
            VehicleInventoryDescription.Slot slot = handler.getVehicle().getInventoryDescription().getSlots().get(focusedSlot.getIndex());
            if (slot.type == VehicleInventoryDescription.SlotType.DYE || slot.type == VehicleInventoryDescription.SlotType.BOOSTER || slot.type == VehicleInventoryDescription.SlotType.BOILER || slot.type == VehicleInventoryDescription.SlotType.UPGRADE || slot.type == VehicleInventoryDescription.SlotType.BANNER || slot.type == VehicleInventoryDescription.SlotType.WEAPON) {
                this.renderTooltip(matrices, List.of(Text.translatable("immersive_aircraft.slot." + slot.type.name().toLowerCase(Locale.ROOT))), Optional.empty(), mouseX, mouseY);
            }
        } else {
            drawMouseoverTooltip(matrices, mouseX, mouseY);
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
            for (Rect2i rectangle : handler.getVehicle().getInventoryDescription().getRectangles()) {
                if (mouseX > rectangle.getX() + x && mouseX < rectangle.getX() + rectangle.getWidth() + x && mouseY > rectangle.getY() + y && mouseY < rectangle.getY() + rectangle.getHeight() + y) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
