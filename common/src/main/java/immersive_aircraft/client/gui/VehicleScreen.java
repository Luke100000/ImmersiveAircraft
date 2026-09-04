package immersive_aircraft.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.client.render.entity.renderer.utils.BBModelRenderer;
import immersive_aircraft.data.VehicleSkin;
import immersive_aircraft.data.VehicleSkinDataLoader;
import immersive_aircraft.entity.inventory.slots.SlotDescription;
import immersive_aircraft.network.c2s.SelectVehicleSkinMessage;
import immersive_aircraft.network.s2c.OpenGuiRequest;
import immersive_aircraft.resources.BBModelLoader;
import immersive_aircraft.resources.bbmodel.BBFace;
import immersive_aircraft.resources.bbmodel.BBAnimationVariables;
import immersive_aircraft.resources.bbmodel.BBFaceContainer;
import immersive_aircraft.resources.bbmodel.BBModel;
import immersive_aircraft.resources.bbmodel.BBObject;
import immersive_aircraft.screen.VehicleScreenHandler;
import immersive_aircraft.util.Rect2iCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class VehicleScreen extends AbstractContainerScreen<VehicleScreenHandler> {
    private static final ResourceLocation TEXTURE = Main.locate("textures/gui/container/inventory.png");

    public static final int TITLE_HEIGHT = 10;
    public static final int BASE_HEIGHT = 86;
    private static final int SKIN_PANEL_WIDTH = 360;
    private static final int SKIN_BUTTON_WIDTH = 88;
    private static final int MAX_SKINS_PER_PAGE = 5;

    public int containerSize;
    private final List<ResourceLocation> availableSkins;
    private final List<ResourceLocation> filteredSkins = new ArrayList<>();
    private final Map<ResourceLocation, Button> skinButtons = new HashMap<>();
    private final Map<ResourceLocation, ModelBounds> modelBoundsCache = new HashMap<>();
    private final List<Button> skinSelectorButtons = new ArrayList<>();
    @Nullable
    private ResourceLocation selectedSkin;
    @Nullable
    private ResourceLocation previewSkin;
    @Nullable
    private EditBox skinSearchBox;
    @Nullable
    private Button applySkinButton;
    private String skinSearch = "";
    private int skinPage;
    private boolean skinSelectorOpen;
    private boolean focusSkinSearch;

    public VehicleScreen(VehicleScreenHandler handler, Inventory inventory, Component title) {
        this(handler, inventory, title, List.of(), null);
    }

    public VehicleScreen(VehicleScreenHandler handler, Inventory inventory, Component title, OpenGuiRequest request) {
        this(handler, inventory, title, request.getAvailableSkins(), request.getSelectedSkin());
    }

    private VehicleScreen(VehicleScreenHandler handler, Inventory inventory, Component title,
                          List<ResourceLocation> availableSkins, @Nullable ResourceLocation selectedSkin) {
        super(handler, inventory, title);

        containerSize = handler.getVehicle().getInventoryDescription().getHeight();
        this.availableSkins = new ArrayList<>(availableSkins);
        this.selectedSkin = selectedSkin;
        this.previewSkin = availableSkins.contains(selectedSkin)
                ? selectedSkin
                : availableSkins.stream().findFirst().orElse(null);
        updateFilteredSkins();

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

    private int getSkinPanelWidth() {
        return Math.min(SKIN_PANEL_WIDTH, width - 16);
    }

    private int getSkinPanelX() {
        return (width - getSkinPanelWidth()) / 2;
    }

    private int getSkinPanelY() {
        return (height - getSkinPanelHeight()) / 2;
    }

    private int getSkinsPerPage() {
        return Math.max(1, Math.min(MAX_SKINS_PER_PAGE, (height - 96) / 22));
    }

    private int getSkinListWidth() {
        int availableWidth = getSkinPanelWidth() - 24;
        return Math.max(96, Math.min(196, availableWidth * 3 / 5));
    }

    private int getSkinContentHeight() {
        int rows = Math.max(1, Math.min(getSkinsPerPage(), filteredSkins.size()));
        return Math.max(64, rows * 22);
    }

    private int getSkinPanelHeight() {
        return 56 + getSkinContentHeight() + 24;
    }

    private Component getSkinName(ResourceLocation skinId) {
        return VehicleSkinDataLoader.getClient(skinId)
                .map(VehicleSkin::displayName)
                .orElseGet(() -> Component.literal(skinId.toString()));
    }

    private void updateFilteredSkins() {
        String query = skinSearch.trim().toLowerCase(Locale.ROOT);
        filteredSkins.clear();
        availableSkins.stream()
                .filter(skinId -> query.isEmpty()
                        || skinId.toString().toLowerCase(Locale.ROOT).contains(query)
                        || getSkinName(skinId).getString().toLowerCase(Locale.ROOT).contains(query))
                .forEach(filteredSkins::add);

        if (!filteredSkins.contains(previewSkin)) {
            previewSkin = filteredSkins.stream().findFirst().orElse(null);
        }
    }

    private void previewSkin(ResourceLocation skinId) {
        previewSkin = skinId;
        skinButtons.forEach((id, button) -> button.active = !id.equals(previewSkin));
        if (applySkinButton != null) {
            applySkinButton.active = !Objects.equals(previewSkin, selectedSkin);
        }
    }

    private void selectSkin(ResourceLocation skinId) {
        selectedSkin = skinId;
        NetworkHandler.sendToServer(new SelectVehicleSkinMessage(menu.getVehicle().getId(), skinId));
        previewSkin(skinId);
    }

    private void setSkinSelectorOpen(boolean open) {
        skinSelectorOpen = open;
        if (open) {
            updateFilteredSkins();
        }
        rebuildSkinButtons();
    }

    private void onSkinSearchChanged(String query) {
        if (skinSearch.equals(query)) {
            return;
        }
        skinSearch = query;
        skinPage = 0;
        focusSkinSearch = true;
        updateFilteredSkins();
        rebuildSkinButtons();
    }

    private void rebuildSkinButtons() {
        clearWidgets();
        skinButtons.clear();
        skinSelectorButtons.clear();
        skinSearchBox = null;
        applySkinButton = null;
        if (availableSkins.isEmpty()) {
            return;
        }

        if (!skinSelectorOpen) {
            addRenderableWidget(Button.builder(
                            Component.translatable("gui.immersive_aircraft.vehicle_skins"),
                            ignored -> setSkinSelectorOpen(true))
                    .bounds(width - SKIN_BUTTON_WIDTH - 6, 6, SKIN_BUTTON_WIDTH, 20)
                    .build());
            return;
        }

        int skinsPerPage = getSkinsPerPage();
        int pageCount = Math.max(1, (filteredSkins.size() + skinsPerPage - 1) / skinsPerPage);
        skinPage = Math.max(0, Math.min(skinPage, pageCount - 1));
        int start = skinPage * skinsPerPage;
        int end = Math.min(filteredSkins.size(), start + skinsPerPage);
        int panelX = getSkinPanelX();
        int panelY = getSkinPanelY();
        int panelWidth = getSkinPanelWidth();
        int listX = panelX + 8;
        int listWidth = getSkinListWidth();
        int contentY = panelY + 50;
        int footerY = contentY + getSkinContentHeight() + 4;

        skinSearchBox = new EditBox(font, panelX + 8, panelY + 27, panelWidth - 16, 18,
                Component.translatable("gui.immersive_aircraft.search_skins"));
        skinSearchBox.setMaxLength(80);
        skinSearchBox.setHint(Component.translatable("gui.immersive_aircraft.search_skins"));
        skinSearchBox.setValue(skinSearch);
        skinSearchBox.setResponder(this::onSkinSearchChanged);
        addRenderableWidget(skinSearchBox);
        if (focusSkinSearch) {
            setFocused(skinSearchBox);
            skinSearchBox.setFocused(true);
            focusSkinSearch = false;
        }

        for (int i = start; i < end; i++) {
            ResourceLocation skinId = filteredSkins.get(i);
            Button button = Button.builder(getSkinName(skinId), ignored -> previewSkin(skinId))
                    .bounds(listX, contentY + (i - start) * 22, listWidth, 20)
                    .build();
            button.active = !skinId.equals(previewSkin);
            Button addedButton = addRenderableWidget(button);
            skinButtons.put(skinId, addedButton);
            skinSelectorButtons.add(addedButton);
        }

        Button close = Button.builder(Component.literal("×"), ignored -> setSkinSelectorOpen(false))
                .bounds(panelX + panelWidth - 24, panelY + 4, 20, 18)
                .build();
        skinSelectorButtons.add(addRenderableWidget(close));

        if (pageCount > 1) {
            Button previous = Button.builder(Component.literal("<"), ignored -> {
                        skinPage = Math.floorMod(skinPage - 1, pageCount);
                        previewSkin = filteredSkins.get(skinPage * skinsPerPage);
                        rebuildSkinButtons();
                    })
                    .bounds(listX, footerY, 28, 20)
                    .build();
            Button next = Button.builder(Component.literal(">"), ignored -> {
                        skinPage = (skinPage + 1) % pageCount;
                        previewSkin = filteredSkins.get(skinPage * skinsPerPage);
                        rebuildSkinButtons();
                    })
                    .bounds(listX + listWidth - 28, footerY, 28, 20)
                    .build();
            skinSelectorButtons.add(addRenderableWidget(previous));
            skinSelectorButtons.add(addRenderableWidget(next));
        }

        int previewX = listX + listWidth + 8;
        int previewWidth = panelX + panelWidth - 8 - previewX;
        applySkinButton = Button.builder(Component.translatable("gui.immersive_aircraft.use_skin"), ignored -> {
                    if (previewSkin != null) {
                        selectSkin(previewSkin);
                    }
                })
                .bounds(previewX, footerY, previewWidth, 20)
                .build();
        applySkinButton.active = previewSkin != null && !Objects.equals(previewSkin, selectedSkin);
        skinSelectorButtons.add(addRenderableWidget(applySkinButton));
    }

    public void drawImage(GuiGraphics context, int x, int y, int u, int v, int w, int h) {
        context.blit(TEXTURE, x, y, u, v, w, h, 512, 256);
    }

    private ModelBounds getModelBounds(BBModel model) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;

        for (BBObject object : model.objects.values()) {
            if (!(object instanceof BBFaceContainer container)) {
                continue;
            }
            for (BBFace face : container.getFaces()) {
                for (BBFace.BBVertex vertex : face.vertices) {
                    float x = vertex.x + object.origin.x();
                    float y = vertex.y + object.origin.y();
                    float z = vertex.z + object.origin.z();
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    minZ = Math.min(minZ, z);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                    maxZ = Math.max(maxZ, z);
                }
            }
        }

        if (minX == Float.MAX_VALUE) {
            return new ModelBounds(0, 0, 0, 1, 1, 1);
        }
        return new ModelBounds(
                (minX + maxX) * 0.5f,
                (minY + maxY) * 0.5f,
                (minZ + maxZ) * 0.5f,
                Math.max(0.01f, maxX - minX),
                Math.max(0.01f, maxY - minY),
                Math.max(0.01f, maxZ - minZ)
        );
    }

    private void renderSkinPreview(GuiGraphics context, int previewX, int previewY,
                                   int previewWidth, int previewHeight, float delta) {
        context.fill(previewX, previewY, previewX + previewWidth, previewY + previewHeight, 0xFF101010);
        if (previewSkin == null) {
            context.drawCenteredString(font, Component.translatable("gui.immersive_aircraft.no_skin_results"),
                    previewX + previewWidth / 2, previewY + previewHeight / 2 - 4, 0xA0A0A0);
            return;
        }

        VehicleSkin skin = VehicleSkinDataLoader.getClient(previewSkin).orElse(null);
        BBModel model = skin == null ? null : BBModelLoader.MODELS.get(skin.model());
        String name = font.plainSubstrByWidth(getSkinName(previewSkin).getString(), previewWidth - 6);
        context.drawCenteredString(font, Component.literal(name), previewX + previewWidth / 2, previewY + 3, 0xFFFFFF);
        if (model == null || previewHeight < 28) {
            context.drawCenteredString(font, Component.translatable("gui.immersive_aircraft.preview_unavailable"),
                    previewX + previewWidth / 2, previewY + previewHeight / 2, 0xFF8080);
            return;
        }

        int modelTop = previewY + 14;
        int modelHeight = previewHeight - 16;
        ModelBounds bounds = modelBoundsCache.computeIfAbsent(model.id, ignored -> getModelBounds(model));
        float horizontalExtent = (float) Math.sqrt(bounds.width() * bounds.width() + bounds.depth() * bounds.depth());
        float verticalExtent = bounds.height() + horizontalExtent * 0.35f;
        float scale = Math.min((previewWidth - 6.0f) / horizontalExtent, (modelHeight - 4.0f) / verticalExtent);

        context.flush();
        context.enableScissor(previewX + 1, modelTop, previewX + previewWidth - 1, previewY + previewHeight - 1);
        RenderSystem.enableDepthTest();
        context.pose().pushPose();
        context.pose().translate(previewX + previewWidth / 2.0f, modelTop + modelHeight / 2.0f, 100);
        context.pose().scale(scale, -scale, scale);
        context.pose().mulPose(Axis.XP.rotationDegrees(20));
        context.pose().mulPose(Axis.YP.rotationDegrees((menu.getVehicle().tickCount + delta) * 0.75f));
        context.pose().translate(-bounds.centerX(), -bounds.centerY(), -bounds.centerZ());

        float time = (menu.getVehicle().level().getGameTime() % 24000 + delta) / 20.0f;
        BBAnimationVariables.set("time", time);
        menu.getVehicle().setAnimationVariables(delta);
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        BBModelRenderer.renderModel(model, context.pose(), buffers, LightTexture.FULL_BRIGHT, time,
                menu.getVehicle(), null, 1.0f, 1.0f, 1.0f, 1.0f);
        buffers.endBatch();

        context.pose().popPose();
        RenderSystem.disableDepthTest();
        context.disableScissor();
    }

    private void renderSkinSelector(GuiGraphics context, int mouseX, int mouseY, float delta) {
        int panelX = getSkinPanelX();
        int panelY = getSkinPanelY();
        int panelWidth = getSkinPanelWidth();
        int panelHeight = getSkinPanelHeight();
        int listX = panelX + 8;
        int listWidth = getSkinListWidth();
        int contentY = panelY + 50;
        int contentHeight = getSkinContentHeight();
        int previewX = listX + listWidth + 8;
        int previewWidth = panelX + panelWidth - 8 - previewX;

        context.pose().pushPose();
        context.pose().translate(0, 0, 400);
        context.fill(0, 0, width, height, 0xA0000000);
        context.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, 0xFF000000);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xF0202020);
        context.drawCenteredString(font, Component.translatable("gui.immersive_aircraft.vehicle_skins"),
                panelX + panelWidth / 2, panelY + 9, 0xFFFFFF);

        if (filteredSkins.isEmpty()) {
            context.drawCenteredString(font, Component.translatable("gui.immersive_aircraft.no_skin_results"),
                    listX + listWidth / 2, contentY + contentHeight / 2 - 4, 0xA0A0A0);
        }

        int skinsPerPage = getSkinsPerPage();
        if (filteredSkins.size() > skinsPerPage) {
            int pageCount = (filteredSkins.size() + skinsPerPage - 1) / skinsPerPage;
            context.drawCenteredString(font, Component.literal((skinPage + 1) + " / " + pageCount),
                    listX + listWidth / 2, panelY + panelHeight - 18, 0xA0A0A0);
        }

        renderSkinPreview(context, previewX, contentY, previewWidth, contentHeight, delta);
        if (skinSearchBox != null) {
            skinSearchBox.render(context, mouseX, mouseY, delta);
        }
        skinSelectorButtons.forEach(button -> button.render(context, mouseX, mouseY, delta));
        context.pose().popPose();
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

        if (skinSelectorOpen) {
            renderSkinSelector(context, mouseX, mouseY, delta);
        } else {
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
    }

    @Override
    protected void init() {
        super.init();

        titleLabelX = (imageWidth - font.width(title)) / 2;
        rebuildSkinButtons();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (skinSelectorOpen) {
            if (skinSearchBox != null && skinSearchBox.mouseClicked(mouseX, mouseY, button)) {
                setFocused(skinSearchBox);
                return true;
            }
            for (Button selectorButton : List.copyOf(skinSelectorButtons)) {
                if (selectorButton.mouseClicked(mouseX, mouseY, button)) {
                    if (skinSearchBox != null) {
                        skinSearchBox.setFocused(false);
                    }
                    setFocused(selectorButton);
                    return true;
                }
            }

            int panelX = getSkinPanelX();
            int panelY = getSkinPanelY();
            if (mouseX < panelX || mouseX >= panelX + getSkinPanelWidth()
                    || mouseY < panelY || mouseY >= panelY + getSkinPanelHeight()) {
                setSkinSelectorOpen(false);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (skinSelectorOpen && keyCode == 256) {
            setSkinSelectorOpen(false);
            return true;
        }
        if (skinSelectorOpen && skinSearchBox != null && skinSearchBox.isFocused()) {
            skinSearchBox.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (skinSelectorOpen && skinSearchBox != null && skinSearchBox.isFocused()) {
            return skinSearchBox.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        if (skinSelectorOpen) {
            return false;
        }
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

    public Optional<Rect2i> getSkinPanelArea() {
        if (availableSkins.isEmpty()) {
            return Optional.empty();
        }
        if (!skinSelectorOpen) {
            return Optional.of(new Rect2i(width - SKIN_BUTTON_WIDTH - 6, 6, SKIN_BUTTON_WIDTH, 20));
        }
        return Optional.of(new Rect2i(getSkinPanelX(), getSkinPanelY(), getSkinPanelWidth(), getSkinPanelHeight()));
    }

    private record ModelBounds(float centerX, float centerY, float centerZ,
                               float width, float height, float depth) {
    }
}
