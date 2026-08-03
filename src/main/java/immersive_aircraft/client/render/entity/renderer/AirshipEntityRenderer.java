package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.AirshipEntity;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.util.Utils;
import immersive_aircraft.util.obj.Mesh;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class AirshipEntityRenderer<T extends AirshipEntity> extends AircraftEntityRenderer<T> {
    private static final ResourceLocation id = Main.locate("objects/airship.obj");

    private final ResourceLocation texture = Main.locate("textures/entity/airship.png");

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
                    new Object(id, "sails")
                            .setRenderConsumer(
                                    (entity, tickDelta) -> {
                                        bindTexture(getTexture(entity));
                                        GlStateManager.disableCull();

                                        ItemStack stack = entity.getSlots(VehicleInventoryDescription.SlotType.DYE).get(0);
                                        EnumDyeColor color;
                                        if (stack.getItem() instanceof ItemDye) {
                                            color = EnumDyeColor.byDyeDamage(stack.getMetadata());
                                        } else {
                                            color = EnumDyeColor.WHITE;
                                        }
                                        float r = color.getColorComponentValues()[0];
                                        float g = color.getColorComponentValues()[1];
                                        float b = color.getColorComponentValues()[2];

                                        if (entity.isWithinParticleRange() && Config.getInstance().enableAnimatedSails) {
                                            Mesh mesh = getFaces(id, "sails_animated");
                                            float time = entity.world.getWorldTime() % 24000 + tickDelta;
                                            renderSailObject(mesh, time, r, g, b, 1.0f);
                                        } else {
                                            Mesh mesh = getFaces(id, "sails");
                                            renderObject(mesh, r, g, b, 1.0f);
                                        }
                                    }
                            )
            )
            .add(
                    new Object(id, "controller").setAnimationConsumer(
                            (entity, yaw, tickDelta) -> {
                                GlStateManager.translate(0, -0.125, 0.78125f);
                                GlStateManager.rotate(-entity.pressingInterpolatedX.getSmooth(tickDelta) * 20.0f, 0.0f, 0.0f, 1.0f);
                                GlStateManager.rotate(entity.pressingInterpolatedZ.getSmooth(tickDelta) * 30.0f, 1.0f, 0.0f, 0.0f);
                                GlStateManager.translate(0, 0.125, -0.78125f - 2.0f / 16.0f);
                            }
                    )
            )
            .add(
                    new Object(id, "propeller")
                            .setAnimationConsumer(
                                    (entity, yaw, tickDelta) -> {
                                        GlStateManager.translate(0.0f, 0.1875f, 0.0f);
                                        GlStateManager.rotate((float)(-entity.engineRotation.getSmooth(tickDelta) * 100.0), 0.0f, 0.0f, 1.0f);
                                        GlStateManager.translate(0.0f, -0.1875f, 0.0f);
                                    }
                            )
                            .setRenderConsumer(
                                    (entity, tickDelta) -> {
                                        bindTexture(getTexture(entity));
                                        GlStateManager.disableCull();
                                        Mesh mesh = getFaces(id, "propeller");
                                        renderObject(mesh, 1.0f, 1.0f, 1.0f, 1.0f);
                                    }
                            )
            );

    public AirshipEntityRenderer(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.8f;
    }

    @Override
    public ResourceLocation getTexture(T entity) {
        return texture;
    }

    @Override
    public Model getModel(AircraftEntity entity) {
        return model;
    }

    @Override
    public immersive_aircraft.compat.Vec3f getPivot(AircraftEntity entity) {
        return new immersive_aircraft.compat.Vec3f(0.0f, 0.2f, 0.0f);
    }
}
