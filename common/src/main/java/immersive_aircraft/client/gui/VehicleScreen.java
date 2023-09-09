package immersive_aircraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.Main;
import immersive_aircraft.entity.EngineAircraft;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.screen.VehicleScreenHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
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
    protected void renderBg(@NotNull PoseStack matrices, float delta, int mouseX, int mouseY) {
        // nop
    }

    protected void drawRectangle(PoseStack matrices, int x, int y, int h, int w) {
        //corners
        blit(matrices, x, y, 176, 0, 16, 16, 512, 256);
        blit(matrices, x + w - 16, y, 176 + 32, 0, 16, 16, 512, 256);
        blit(matrices, x + w - 16, y + h - 16, 176 + 32, 32, 16, 16, 512, 256);
        blit(matrices, x, y + h - 16, 176, 32, 16, 16, 512, 256);

        //edges
        blit(matrices, x + 16, y, w - 32, 16, 176 + 16, 0, 16, 16, 512, 256);
        blit(matrices, x + 16, y + h - 16, w - 32, 16, 176 + 16, 32, 16, 16, 512, 256);
        blit(matrices, x, y + 16, 16, h - 32, 176, 16, 16, 16, 512, 256);
        blit(matrices, x + w - 16, y + 16, 16, h - 32, 176 + 32, 16, 16, 16, 512, 256);

        //center
        blit(matrices, x + 16, y + 16, w - 32, h - 32, 176 + 16, 16, 16, 16, 512, 256);
    }

    protected void drawCustomBackground(PoseStack matrices) {
        this.renderBackground(matrices);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        blit(matrices, leftPos, topPos, 0, 0, imageWidth, containerSize + TITLE_HEIGHT * 2, 512, 256);
        blit(matrices, leftPos, topPos + containerSize + TITLE_HEIGHT * 2 - 4, 0, 222 - BASE_HEIGHT, imageWidth, BASE_HEIGHT, 512, 256);

        for (Rect2i rectangle : menu.getVehicle().getInventoryDescription().getRectangles()) {
            drawRectangle(matrices, leftPos + rectangle.getX(), topPos + rectangle.getY(), rectangle.getWidth(), rectangle.getHeight());
        }
    }

    private void drawImage(PoseStack matrices, int x, int y, int u, int v, int w, int h) {
        blit(matrices, x, y, u, v, w, h, 512, 256);
    }

    @Override
    public void render(@NotNull PoseStack matrices, int mouseX, int mouseY, float delta) {
        drawCustomBackground(matrices);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int titleHeight = 10;

        for (VehicleInventoryDescription.Slot slot : menu.getVehicle().getInventoryDescription().getSlots()) {
            switch (slot.type) {
                case INVENTORY ->
                        drawImage(matrices, leftPos + slot.x - 1, topPos + titleHeight + slot.y - 1, 284, 0, 18, 18);
                case BOILER -> {
                    drawImage(matrices, this.leftPos + slot.x - 4, this.topPos + titleHeight + slot.y - 18, 318, 0, 24, 39);
                    if (menu.getVehicle() instanceof EngineAircraft engineAircraft && engineAircraft.getFuelUtilization() > 0.0) {
                        drawImage(matrices, this.leftPos + slot.x - 4, this.topPos + titleHeight + slot.y - 18, 318 + 30, 0, 24, 39);
                    }
                }
                default -> {
                    if (menu.getVehicle().getInventory().getItem(slot.index).isEmpty()) {
                        switch (slot.type) {
                            case WEAPON ->
                                    drawImage(matrices, leftPos + slot.x - 3, topPos + titleHeight + slot.y - 3, 262, 22, 22, 22);
                            case UPGRADE ->
                                    drawImage(matrices, leftPos + slot.x - 3, topPos + titleHeight + slot.y - 3, 262, 22 * 2, 22, 22);
                            case BANNER ->
                                    drawImage(matrices, leftPos + slot.x - 3, topPos + titleHeight + slot.y - 3, 262, 22 * 3, 22, 22);
                            case DYE ->
                                    drawImage(matrices, leftPos + slot.x - 3, topPos + titleHeight + slot.y - 3, 262, 22 * 4, 22, 22);
                            case BOOSTER ->
                                    drawImage(matrices, leftPos + slot.x - 3, topPos + titleHeight + slot.y - 3, 262, 22 * 5, 22, 22);
                        }
                    } else {
                        drawImage(matrices, this.leftPos + slot.x - 3, this.topPos + titleHeight + slot.y - 3, 262, 0, 22, 22);
                    }
                }
            }
        }

        super.render(matrices, mouseX, mouseY, delta);

        // Slot tooltip
        if (hoveredSlot != null && !hoveredSlot.hasItem() && hoveredSlot.container == menu.getVehicle().getInventory()) {
            VehicleInventoryDescription.Slot slot = menu.getVehicle().getInventoryDescription().getSlots().get(hoveredSlot.getContainerSlot());
            if (slot.type == VehicleInventoryDescription.SlotType.DYE || slot.type == VehicleInventoryDescription.SlotType.BOOSTER || slot.type == VehicleInventoryDescription.SlotType.BOILER || slot.type == VehicleInventoryDescription.SlotType.UPGRADE || slot.type == VehicleInventoryDescription.SlotType.BANNER || slot.type == VehicleInventoryDescription.SlotType.WEAPON) {
                this.renderTooltip(matrices, List.of(Component.translatable("immersive_aircraft.slot." + slot.type.name().toLowerCase(Locale.ROOT))), Optional.empty(), mouseX, mouseY);
            }
        } else {
            renderTooltip(matrices, mouseX, mouseY);
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
            for (Rect2i rectangle : menu.getVehicle().getInventoryDescription().getRectangles()) {
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
