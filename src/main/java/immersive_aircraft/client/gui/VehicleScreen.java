package immersive_aircraft.client.gui;

import immersive_aircraft.Main;
import immersive_aircraft.entity.EngineAircraft;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.screen.VehicleScreenHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Collections;
import java.util.Locale;

/**
 * 1.12.2 port of the 1.16 VehicleScreen: MatrixStack drawing replaced by
 * drawModalRectWithCustomSizedTexture against the 512x256 gui texture.
 */
public class VehicleScreen extends GuiContainer {
    private static final ResourceLocation TEXTURE = Main.locate("textures/gui/container/inventory.png");

    public static int titleHeight = 10;
    public static int baseHeight = 86;
    public static int containerSize;

    private final VehicleScreenHandler handler;

    public VehicleScreen(VehicleScreenHandler handler) {
        super(handler);

        this.handler = handler;

        containerSize = handler.getVehicle().getInventoryDescription().getHeight();

        ySize = baseHeight + containerSize + titleHeight * 2;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        //nop
    }

    protected void drawRectangle(int x, int y, int h, int w) {
        //corners
        drawModalRectWithCustomSizedTexture(x, y, 176, 0, 16, 16, 512, 256);
        drawModalRectWithCustomSizedTexture(x + w - 16, y, 176 + 32, 0, 16, 16, 512, 256);
        drawModalRectWithCustomSizedTexture(x + w - 16, y + h - 16, 176 + 32, 32, 16, 16, 512, 256);
        drawModalRectWithCustomSizedTexture(x, y + h - 16, 176, 32, 16, 16, 512, 256);

        //edges
        drawScaledCustomSizeModalRect(x + 16, y, 176 + 16, 0, 16, 16, w - 32, 16, 512, 256);
        drawScaledCustomSizeModalRect(x + 16, y + h - 16, 176 + 16, 32, 16, 16, w - 32, 16, 512, 256);
        drawScaledCustomSizeModalRect(x, y + 16, 176, 16, 16, 16, 16, h - 32, 512, 256);
        drawScaledCustomSizeModalRect(x + w - 16, y + 16, 176 + 32, 16, 16, 16, 16, h - 32, 512, 256);

        //center
        drawScaledCustomSizeModalRect(x + 16, y + 16, 176 + 16, 16, 16, 16, w - 32, h - 32, 512, 256);
    }

    protected void drawCustomBackground() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);

        drawModalRectWithCustomSizedTexture(guiLeft, guiTop, 0, 0, xSize, containerSize + titleHeight * 2, 512, 256);
        drawModalRectWithCustomSizedTexture(guiLeft, guiTop + containerSize + titleHeight * 2 - 4, 0, 222 - baseHeight, xSize, baseHeight, 512, 256);

        for (VehicleInventoryDescription.Rectangle rectangle : handler.getVehicle().getInventoryDescription().getRectangles()) {
            drawRectangle(guiLeft + rectangle.x(), guiTop + rectangle.y(), rectangle.w(), rectangle.h());
        }
    }

    private void drawImage(int x, int y, int u, int v, int w, int h) {
        drawModalRectWithCustomSizedTexture(x, y, u, v, w, h, 512, 256);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawCustomBackground();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);

        int titleHeight = 10;

        for (VehicleInventoryDescription.Slot slot : handler.getVehicle().getInventoryDescription().getSlots()) {
            switch (slot.type) {
                case INVENTORY:
                    drawImage(guiLeft + slot.x - 1, guiTop + titleHeight + slot.y - 1, 284, 0, 18, 18);
                    break;
                case BOILER: {
                    drawImage(guiLeft + slot.x - 4, guiTop + titleHeight + slot.y - 18, 318, 0, 24, 39);
                    if (handler.getVehicle() instanceof EngineAircraft && ((EngineAircraft)handler.getVehicle()).getFuelUtilization() > 0.0) {
                        drawImage(guiLeft + slot.x - 4, guiTop + titleHeight + slot.y - 18, 318 + 30, 0, 24, 39);
                    }
                    break;
                }
                default: {
                    if (handler.getVehicle().getInventory().getStack(slot.index).isEmpty()) {
                        switch (slot.type) {
                            case WEAPON:
                                drawImage(guiLeft + slot.x - 3, guiTop + titleHeight + slot.y - 3, 262, 22, 22, 22);
                                break;
                            case UPGRADE:
                                drawImage(guiLeft + slot.x - 3, guiTop + titleHeight + slot.y - 3, 262, 22 * 2, 22, 22);
                                break;
                            case BANNER:
                                drawImage(guiLeft + slot.x - 3, guiTop + titleHeight + slot.y - 3, 262, 22 * 3, 22, 22);
                                break;
                            case DYE:
                                drawImage(guiLeft + slot.x - 3, guiTop + titleHeight + slot.y - 3, 262, 22 * 4, 22, 22);
                                break;
                            case BOOSTER:
                                drawImage(guiLeft + slot.x - 3, guiTop + titleHeight + slot.y - 3, 262, 22 * 5, 22, 22);
                                break;
                        }
                    } else {
                        drawImage(guiLeft + slot.x - 3, guiTop + titleHeight + slot.y - 3, 262, 0, 22, 22);
                    }
                }
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        // Slot tooltip
        Slot focusedSlot = getSlotUnderMouse();
        if (focusedSlot != null && !focusedSlot.getHasStack() && focusedSlot.inventory == handler.getVehicle().getInventory()) {
            VehicleInventoryDescription.Slot slot = handler.getVehicle().getInventoryDescription().getSlots().get(focusedSlot.getSlotIndex());
            if (slot.type == VehicleInventoryDescription.SlotType.DYE || slot.type == VehicleInventoryDescription.SlotType.BOOSTER || slot.type == VehicleInventoryDescription.SlotType.BOILER || slot.type == VehicleInventoryDescription.SlotType.UPGRADE || slot.type == VehicleInventoryDescription.SlotType.BANNER || slot.type == VehicleInventoryDescription.SlotType.WEAPON) {
                drawHoveringText(Collections.singletonList(new TextComponentTranslation("immersive_aircraft.slot." + slot.type.name().toLowerCase(Locale.ROOT)).getFormattedText()), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = handler.getVehicle().getDisplayName().getFormattedText();
        fontRenderer.drawString(title, (xSize - fontRenderer.getStringWidth(title)) / 2, 6, 4210752);
        fontRenderer.drawString(net.minecraft.client.resources.I18n.format("container.inventory"), 8, ySize - 96 + 2, 4210752);
    }

    @Override
    protected boolean hasClickedOutside(int mouseX, int mouseY, int guiLeftIn, int guiTopIn) {
        if (super.hasClickedOutside(mouseX, mouseY, guiLeftIn, guiTopIn)) {
            for (VehicleInventoryDescription.Rectangle rectangle : handler.getVehicle().getInventoryDescription().getRectangles()) {
                if (mouseX > rectangle.x() + guiLeft && mouseX < rectangle.x() + rectangle.w() + guiLeft && mouseY > rectangle.y() + guiTop && mouseY < rectangle.y() + rectangle.h() + guiTop) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
