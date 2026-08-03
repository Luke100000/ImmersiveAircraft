package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.BiplaneEntity;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.util.Utils;
import immersive_aircraft.util.obj.Mesh;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class BiplaneEntityRenderer<T extends BiplaneEntity> extends AircraftEntityRenderer<T> {
    private static final ResourceLocation id = Main.locate("objects/biplane.obj");

    private final ResourceLocation texture;

    private final Model model = new Model()
            .add(
                    new Object(id, "frame")
            )
            .add(
                    new Object(id, "banners").setRenderConsumer(
                            (entity, tickDelta) -> {
                                List<ItemStack> slots = entity.getSlots(VehicleInventoryDescription.SlotType.BANNER);
                                int i = 0;
                                for (ItemStack slot : slots) {
                                    if (!slot.isEmpty() && slot.getItem() instanceof ItemBanner) {
                                        List<?> patterns = Utils.parseBannerItem(slot);
                                        Mesh mesh = getFaces(id, "banner_" + (i++));
                                        renderBanner(mesh, patterns);
                                    }
                                }
                            }
                    )
            )
            .add(
                    new Object(id, "propeller").setAnimationConsumer(
                            (entity, yaw, tickDelta) -> {
                                GlStateManager.translate(0.0f, 0.3125f, 0.0f);
                                GlStateManager.rotate((float)(entity.engineRotation.getSmooth(tickDelta) * 100.0), 0.0f, 0.0f, 1.0f);
                                GlStateManager.translate(0.0f, -0.3125f, 0.0f);
                            }
                    )
            )
            .add(
                    new Object(id, "elevator").setAnimationConsumer(
                            (entity, yaw, tickDelta) -> {
                                GlStateManager.translate(0.0f, 0.0625f, -2.5f);
                                GlStateManager.rotate(-entity.pressingInterpolatedZ.getSmooth(tickDelta) * 20.0f, 1.0f, 0.0f, 0.0f);
                                GlStateManager.translate(0.0f, -0.0625f, 2.5f);
                            }
                    )
            )
            .add(
                    new Object(id, "rudder").setAnimationConsumer(
                            (entity, yaw, tickDelta) -> {
                                GlStateManager.translate(0.0f, 0.0625f, -2.5f);
                                GlStateManager.rotate(entity.pressingInterpolatedX.getSmooth(tickDelta) * 18.0f, 0.0f, 1.0f, 0.0f);
                                GlStateManager.translate(0.0f, -0.0625f, 2.5f);
                            }
                    )
            );

    public BiplaneEntityRenderer(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.8f;
        texture = Main.locate("textures/entity/biplane.png");
    }

    @Override
    public ResourceLocation getTexture(T entity) {
        return texture;
    }

    @Override
    protected Model getModel(AircraftEntity entity) {
        return model;
    }

    @Override
    protected immersive_aircraft.compat.Vec3f getPivot(AircraftEntity entity) {
        return new immersive_aircraft.compat.Vec3f(0.0f, 0.4f, 0.05f);
    }
}
