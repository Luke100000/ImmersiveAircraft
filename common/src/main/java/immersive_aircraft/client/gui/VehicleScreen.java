package immersive_aircraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import immersive_aircraft.Main;
import immersive_aircraft.entity.inventory.slots.SlotDescription;
import immersive_aircraft.screen.VehicleScreenHandler;
import immersive_aircraft.util.Rect2iCommon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class VehicleScreen extends AbstractContainerScreen<VehicleScreenHandler> {
    private static final ResourceLocation TEXTURE = Main.locate("textures/gui/container/inventory.png");

    public static final int TITLE_HEIGHT = 10;
    public static final int BASE_HEIGHT = 86;

    public int containerSize;

    public VehicleScreen(VehicleScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);

        containerSize = handler.getVehicle().getInventoryDescription().getHeight();

        imageHeight = BASE_HEIGHT + containerSize + TITLE_HEIGHT * 2;
        inventoryLabelY = containerSize + TITLE_HEIGHT;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics context, float delta, int mouseX, int mouseY) {
        //nop
    }

    protected void drawRectangle(GuiGraphics context, int x, int y, int h, int w) {
        //corners
        context.blit(TEXTURE, x, y, 176, 0, 16, 16, 512, 256);
        context.blit(TEXTURE, x + w - 16, y, 176 + 32, 0, 16, 16, 512, 256);
        context.blit(TEXTURE, x + w - 16, y + h - 16, 176 + 32, 32, 16, 16, 512, 256);
        context.blit(TEXTURE, x, y + h - 16, 176, 32, 16, 16, 512, 256);

        //edges
        context.blit(TEXTURE, x + 16, y, w - 32, 16, 176 + 16, 0, 16, 16, 512, 256);
        context.blit(TEXTURE, x + 16, y + h - 16, w - 32, 16, 176 + 16, 32, 16, 16, 512, 256);
        context.blit(TEXTURE, x, y + 16, 16, h - 32, 176, 16, 16, 16, 512, 256);
        context.blit(TEXTURE, x + w - 16, y + 16, 16, h - 32, 176 + 32, 16, 16, 16, 512, 256);

        //center
        context.blit(TEXTURE, x + 16, y + 16, w - 32, h - 32, 176 + 16, 16, 16, 16, 512, 256);
    }

    protected void drawCustomBackground(GuiGraphics context) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        context.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, containerSize + TITLE_HEIGHT * 2, 512, 256);
        context.blit(TEXTURE, leftPos, topPos + containerSize + TITLE_HEIGHT * 2 - 4, 0, 222 - BASE_HEIGHT, imageWidth, BASE_HEIGHT, 512, 256);

        for (Rect2iCommon rectangle : menu.getVehicle().getInventoryDescription().getRectangles()) {
            drawRectangle(context, leftPos + rectangle.getX(), topPos + rectangle.getY(), rectangle.getHeight(), rectangle.getWidth());
        }
    }

    public void drawImage(GuiGraphics context, int x, int y, int u, int v, int w, int h) {
        context.blit(TEXTURE, x, y, u, v, w, h, 512, 256);
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        renderBackground(context);

        drawCustomBackground(context);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        for (SlotDescription slot : menu.getVehicle().getInventoryDescription().getSlots()) {
            SlotRenderer.get(slot.type()).render(this, context, slot, mouseX, mouseY, delta);
        }

        super.render(context, mouseX, mouseY, delta);

        // Slot tooltip
        if (hoveredSlot != null && !hoveredSlot.hasItem() && hoveredSlot.container == menu.getVehicle().getInventory()) {
            SlotDescription slot = menu.getVehicle().getInventoryDescription().getSlots().get(hoveredSlot.getContainerSlot());
            slot.getToolTip().ifPresent(
                tooltip -> context.renderTooltip(this.font, tooltip, Optional.empty(), mouseX, mouseY)
            );
        } else {
            renderTooltip(context, mouseX, mouseY);
        }
    }

    @Override
    protected void init() {
        super.init();

        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        if (super.hasClickedOutside(mouseX, mouseY, left, top, button)) {
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
