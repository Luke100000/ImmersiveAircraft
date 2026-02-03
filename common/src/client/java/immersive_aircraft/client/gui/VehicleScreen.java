package immersive_aircraft.client.gui;

import immersive_aircraft.Main;
import immersive_aircraft.entity.inventory.slots.SlotDescription;
import immersive_aircraft.screen.VehicleScreenHandler;
import immersive_aircraft.util.Rect2iCommon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class VehicleScreen extends AbstractContainerScreen<VehicleScreenHandler> {
    private static final Identifier TEXTURE = Main.locate("textures/gui/container/inventory.png");

    public static final int TITLE_HEIGHT = 10;
    public static final int BASE_HEIGHT = 86;

    public int containerSize;

    public VehicleScreen(VehicleScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);

        containerSize = handler.getVehicle().getInventoryDescription().getHeight();

        imageHeight = BASE_HEIGHT + containerSize + TITLE_HEIGHT * 2;
        inventoryLabelY = containerSize + TITLE_HEIGHT;
    }

    protected void drawRectangle(GuiGraphics context, int x, int y, int h, int w) {
        //corners
        blit(context, x, y, 16, 16, 176, 0, 512, 256);
        blit(context, x + w - 16, y, 16, 16, 176 + 32, 0, 512, 256);
        blit(context, x + w - 16, y + h - 16, 16, 16, 176 + 32, 32, 512, 256);
        blit(context, x, y + h - 16, 16, 16, 176, 32, 512, 256);

        //edges (stretch 16x16 slice)
        blitStretch(context, x + 16, y, w - 32, 16, 176 + 16, 0);
        blitStretch(context, x + 16, y + h - 16, w - 32, 16, 176 + 16, 32);
        blitStretch(context, x, y + 16, 16, h - 32, 176, 16);
        blitStretch(context, x + w - 16, y + 16, 16, h - 32, 176 + 32, 16);

        //center (stretch 16x16 slice)
        blitStretch(context, x + 16, y + 16, w - 32, h - 32, 176 + 16, 16);
    }

    public void drawImage(GuiGraphics context, int x, int y, int u, int v, int w, int h) {
        blit(context, x, y, w, h, u, v, 512, 256);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics context, float delta, int mouseX, int mouseY) {
        blit(context, leftPos, topPos, imageWidth, containerSize + TITLE_HEIGHT * 2, 0, 0, 512, 256);
        blit(context, leftPos, topPos + containerSize + TITLE_HEIGHT * 2 - 4, imageWidth, BASE_HEIGHT, 0, 222 - BASE_HEIGHT, 512, 256);

        // Draw extra boxed panels on top.
        for (Rect2iCommon rectangle : menu.getVehicle().getInventoryDescription().getRectangles()) {
            drawRectangle(context, leftPos + rectangle.getX(), topPos + rectangle.getY(), rectangle.getHeight(), rectangle.getWidth());
        }

        // Slots
        for (SlotDescription slot : menu.getVehicle().getInventoryDescription().getSlots()) {
            SlotRenderer.get(slot.type()).render(this, context, slot, mouseX, mouseY, delta);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Slot tooltip
        if (hoveredSlot != null && !hoveredSlot.hasItem() && hoveredSlot.container == menu.getVehicle().getInventory()) {
            SlotDescription slot = menu.getVehicle().getInventoryDescription().getSlots().get(hoveredSlot.getContainerSlot());
            slot.getToolTip().ifPresent(
                tooltip -> context.setTooltipForNextFrame(this.font, tooltip, Optional.empty(), mouseX, mouseY)
            );
        } else {
            renderTooltip(context, mouseX, mouseY);
        }
    }

    @Override
    protected void init() {
        super.init();

        titleLabelX = (imageWidth - font.width(title)) / 2;
        recenterForExtraRectangles();
    }

    private void recenterForExtraRectangles() {
        int minX = 0;
        int maxX = imageWidth;
        int minY = 0;
        int maxY = imageHeight;

        for (Rect2iCommon rectangle : menu.getVehicle().getInventoryDescription().getRectangles()) {
            minX = Math.min(minX, rectangle.getX());
            maxX = Math.max(maxX, rectangle.getX() + rectangle.getWidth());
            minY = Math.min(minY, rectangle.getY());
            maxY = Math.max(maxY, rectangle.getY() + rectangle.getHeight());
        }

        int totalWidth = maxX - minX;
        int totalHeight = maxY - minY;
        leftPos = (width - totalWidth) / 2 - minX;
        topPos = (height - totalHeight) / 2 - minY;
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
        if (super.hasClickedOutside(mouseX, mouseY, left, top)) {
            for (Rect2iCommon rectangle : menu.getVehicle().getInventoryDescription().getRectangles()) {
                if (mouseX > rectangle.getX() + leftPos && mouseX < rectangle.getX() + rectangle.getWidth() + leftPos && mouseY > rectangle.getY() + topPos && mouseY < rectangle.getY() + rectangle.getHeight() + topPos) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public int getX() {
        return leftPos;
    }

    public int getY() {
        return topPos;
    }

    public VehicleScreenHandler getMenu() {
        return menu;
    }

    private void blit(GuiGraphics context, int x, int y, int w, int h, int u, int v, int texW, int texH) {
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, (float) u, (float) v, w, h, texW, texH);
    }

    private void blitStretch(GuiGraphics context, int x, int y, int w, int h, int u, int v) {
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, (float) u, (float) v, w, h, 16, 16, 512, 256);
    }
}
