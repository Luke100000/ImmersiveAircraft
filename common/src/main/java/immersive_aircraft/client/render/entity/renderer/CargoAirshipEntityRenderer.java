package immersive_aircraft.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import immersive_aircraft.Main;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.AirshipEntity;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.util.Utils;
import immersive_aircraft.util.obj.Mesh;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;

public class CargoAirshipEntityRenderer<T extends AirshipEntity> extends AirshipEntityRenderer<T> {
    private static final ResourceLocation id = Main.locate("objects/cargo_airship.obj");

    private final ResourceLocation texture = Main.locate("textures/entity/cargo_airship.png");

    private final Model model = new Model()
            .add(
                    new Object(id, "frame")
            )
            .add(
                    new Object(id, "storage")
            )
            .add(
                    new Object(id, "banners").setRenderConsumer(
                            (vertexConsumerProvider, entity, matrixStack, light, tickDelta) -> {
                                List<ItemStack> slots = entity.getSlots(VehicleInventoryDescription.SlotType.BANNER);
                                int i = 0;
                                for (ItemStack slot : slots) {
                                    if (!slot.isEmpty() && slot.getItem() instanceof BannerItem) {
                                        List<Pair<Holder<BannerPattern>, DyeColor>> patterns = Utils.parseBannerItem(slot);
                                        Mesh mesh = getFaces(id, "banner_" + (i++));
                                        renderBanner(matrixStack, vertexConsumerProvider, light, mesh, true, patterns);
                                    }
                                }
                            }
                    )
            )
            .add(
                    new Object(id, "sails")
                            .setRenderConsumer(
                                    (vertexConsumerProvider, entity, matrixStack, light, tickDelta) -> {
                                        ResourceLocation identifier = getTextureLocation(entity);
                                        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.entityCutoutNoCull(identifier));

                                        ItemStack stack = entity.getSlots(VehicleInventoryDescription.SlotType.DYE).get(0);
                                        DyeColor color;
                                        if (stack.getItem() instanceof DyeItem item) {
                                            color = item.getDyeColor();
                                        } else {
                                            color = DyeColor.WHITE;
                                        }
                                        float r = color.getTextureDiffuseColors()[0];
                                        float g = color.getTextureDiffuseColors()[1];
                                        float b = color.getTextureDiffuseColors()[2];

                                        if (entity.isWithinParticleRange() && Config.getInstance().enableAnimatedSails) {
                                            Mesh mesh = getFaces(id, "sails_animated");
                                            float time = entity.level().getGameTime() % 24000 + tickDelta;
                                            renderSailObject(mesh, matrixStack, vertexConsumer, light, time, r, g, b, 1.0f);
                                        } else {
                                            Mesh mesh = getFaces(id, "sails");
                                            renderObject(mesh, matrixStack, vertexConsumer, light, r, g, b, 1.0f);
                                        }
                                    }
                            )
            )
            .add(
                    new Object(id, "controller").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(0, -0.125, 0.78125f);
                                matrixStack.mulPose(Axis.ZP.rotationDegrees(-entity.pressingInterpolatedX.getSmooth(tickDelta) * 20.0f));
                                matrixStack.mulPose(Axis.XP.rotationDegrees(entity.pressingInterpolatedZ.getSmooth(tickDelta) * 30.0f));
                                matrixStack.translate(0, 0.125, -0.78125f - 2.0f / 16.0f);
                            }
                    )
            )
            .add(
                    new Object(id, "propeller")
                            .setAnimationConsumer(
                                    (entity, yaw, tickDelta, matrixStack) -> {
                                        matrixStack.translate(0.0f, 0.1875f, 0.0f);
                                        matrixStack.mulPose(Axis.ZP.rotationDegrees((float) (-entity.engineRotation.getSmooth(tickDelta) * 100.0)));
                                        matrixStack.translate(0.0f, -0.1875f, 0.0f);
                                    }
                            )
                            .setRenderConsumer(
                                    (vertexConsumerProvider, entity, matrixStack, light, tickDelta) -> {
                                        ResourceLocation identifier = getTextureLocation(entity);
                                        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.entityCutoutNoCull(identifier));
                                        Mesh mesh = getFaces(id, "propeller");
                                        renderObject(mesh, matrixStack, vertexConsumer, light);
                                    }
                            )
            )
            .add(
                    new Object(id, "small_propeller_right")
                            .setAnimationConsumer(
                                    (entity, yaw, tickDelta, matrixStack) -> {
                                        matrixStack.translate(-1.15625, 2.34375, 0.0);
                                        matrixStack.mulPose(Axis.ZP.rotationDegrees((float) (entity.engineRotation.getSmooth(tickDelta) * 170.0)));
                                        matrixStack.translate(1.15625, -2.34375, 0.0f);
                                    }
                            )
                            .setRenderConsumer(
                                    (vertexConsumerProvider, entity, matrixStack, light, tickDelta) -> {
                                        ResourceLocation identifier = getTextureLocation(entity);
                                        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.entityCutoutNoCull(identifier));
                                        Mesh mesh = getFaces(id, "small_propeller_right");
                                        renderObject(mesh, matrixStack, vertexConsumer, light);
                                    }
                            )
            )
            .add(
                    new Object(id, "small_propeller_left")
                            .setAnimationConsumer(
                                    (entity, yaw, tickDelta, matrixStack) -> {
                                        matrixStack.translate(1.15625, 2.34375, 0.0);
                                        matrixStack.mulPose(Axis.ZP.rotationDegrees((float) (entity.engineRotation.getSmooth(tickDelta) * 170.0)));
                                        matrixStack.translate(-1.15625, -2.34375, 0.0f);
                                    }
                            )
                            .setRenderConsumer(
                                    (vertexConsumerProvider, entity, matrixStack, light, tickDelta) -> {
                                        ResourceLocation identifier = getTextureLocation(entity);
                                        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.entityCutoutNoCull(identifier));
                                        Mesh mesh = getFaces(id, "small_propeller_left");
                                        renderObject(mesh, matrixStack, vertexConsumer, light);
                                    }
                            )
            );

    public CargoAirshipEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
    }

    @Override
    public ResourceLocation getTextureLocation(@NotNull T aircraft) {
        return texture;
    }

    @Override
    protected Model getModel(AircraftEntity entity) {
        return model;
    }

    @Override
    protected Vector3f getPivot(AircraftEntity entity) {
        return new Vector3f(0.0f, 0.2f, 0.0f);
    }
}
