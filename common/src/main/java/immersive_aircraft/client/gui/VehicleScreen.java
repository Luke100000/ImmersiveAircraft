package immersive_aircraft.client.gui;

import immersive_aircraft.Main;
import immersive_aircraft.entity.inventory.slots.SlotDescription;
import immersive_aircraft.screen.VehicleScreenHandler;
import immersive_aircraft.util.Rect2iCommon;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
        super(handler, inventory, title, 176, BASE_HEIGHT + handler.getVehicle().getInventoryDescription().getHeight() + TITLE_HEIGHT * 2);

        containerSize = handler.getVehicle().getInventoryDescription().getHeight();
        inventoryLabelY = containerSize + TITLE_HEIGHT;
    }

    protected void drawRectangle(GuiGraphicsExtractor context, int x, int y, int h, int w) {
        //corners
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 176, 0, 16, 16, 512, 256);
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + w - 16, y, 176 + 32, 0, 16, 16, 512, 256);
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + w - 16, y + h - 16, 176 + 32, 32, 16, 16, 512, 256);
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + h - 16, 176, 32, 16, 16, 512, 256);

        //edges
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 16, y, 176 + 16, 0, w - 32, 16, 16, 16, 512, 256);
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 16, y + h - 16, 176 + 16, 32, w - 32, 16, 16, 16, 512, 256);
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + 16, 176, 16, 16, h - 32, 16, 16, 512, 256);
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + w - 16, y + 16, 176 + 32, 16, 16, h - 32, 16, 16, 512, 256);

        //center
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 16, y + 16, 176 + 16, 16, w - 32, h - 32, 16, 16, 512, 256);
    }

    public void drawImage(GuiGraphicsExtractor context, int x, int y, int u, int v, int w, int h) {
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, u, v, w, h, 512, 256);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractBackground(context, mouseX, mouseY, delta);
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, containerSize + TITLE_HEIGHT * 2, 512, 256);
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos + containerSize + TITLE_HEIGHT * 2 - 4, 0, 222 - BASE_HEIGHT, imageWidth, BASE_HEIGHT, 512, 256);

        for (Rect2iCommon rectangle : menu.getVehicle().getInventoryDescription().getRectangles()) {
            drawRectangle(context, leftPos + rectangle.getX(), topPos + rectangle.getY(), rectangle.getHeight(), rectangle.getWidth());
        }

        // Slots
        for (SlotDescription slot : menu.getVehicle().getInventoryDescription().getSlots()) {
            SlotRenderer.get(slot.type()).render(this, context, slot, mouseX, mouseY, delta);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        // Slot tooltip
        if (hoveredSlot != null && !hoveredSlot.hasItem() && hoveredSlot.container == menu.getVehicle().getInventory()) {
            SlotDescription slot = menu.getVehicle().getInventoryDescription().getSlots().get(hoveredSlot.getContainerSlot());
            slot.getToolTip().ifPresent(
                tooltip -> context.setTooltipForNextFrame(this.font, tooltip, Optional.empty(), mouseX, mouseY)
            );
        }
    }

    @Override
    protected void init() {
        super.init();

        titleLabelX = (imageWidth - font.width(title)) / 2;
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
}
