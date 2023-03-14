package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.AirshipEntity;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.util.obj.Mesh;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

public class AirshipEntityRenderer<T extends AirshipEntity> extends AircraftEntityRenderer<T> {
    private static final Identifier id = Main.locate("objects/airship.obj");

    private final Identifier texture = Main.locate("textures/entity/airship.png");

    private final Model model = new Model()
            .add(
                    new Object(id, "frame")
            )
            .add(
                    new Object(id, "sails")
                            .setRenderConsumer(
                                    (vertexConsumerProvider, entity, matrixStack, light) -> {
                                        Identifier identifier = getTexture(entity);
                                        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(identifier));

                                        ItemStack stack = entity.getSlots(VehicleInventoryDescription.SlotType.DYE).get(0);
                                        DyeColor color;
                                        if (stack.getItem() instanceof DyeItem item) {
                                            color = item.getColor();
                                        } else {
                                            color = DyeColor.WHITE;
                                        }
                                        float r = color.getColorComponents()[0];
                                        float g = color.getColorComponents()[1];
                                        float b = color.getColorComponents()[2];

                                        if (entity.isWithinParticleRange() && Config.getInstance().enableAnimatedSails) {
                                            Mesh mesh = getFaces(id, "sails_animated");
                                            float time = entity.world.getTime() % 24000 + MinecraftClient.getInstance().getTickDelta();
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
                                matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(-entity.pressingInterpolatedX.getSmooth(tickDelta) * 20.0f));
                                matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(entity.pressingInterpolatedZ.getSmooth(tickDelta) * 30.0f));
                                matrixStack.translate(0, 0.125, -0.78125f - 2.0f / 16.0f);
                            }
                    )
            )
            .add(
                    new Object(id, "propeller")
                            .setAnimationConsumer(
                                    (entity, yaw, tickDelta, matrixStack) -> {
                                        matrixStack.translate(0.0f, 0.1875f, 0.0f);
                                        matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion((float)(-entity.engineRotation.getSmooth(tickDelta) * 100.0)));
                                        matrixStack.translate(0.0f, -0.1875f, 0.0f);
                                    }
                            )
                            .setRenderConsumer(
                                    (vertexConsumerProvider, entity, matrixStack, light) -> {
                                        Identifier identifier = getTexture(entity);
                                        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(identifier));
                                        Mesh mesh = getFaces(id, "propeller");
                                        renderObject(mesh, matrixStack, vertexConsumer, light);
                                    }
                            )
            );

    public AirshipEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
    }

    @Override
    public Identifier getTexture(T AircraftEntity) {
        return texture;
    }

    @Override
    Model getModel(AircraftEntity entity) {
        return model;
    }

    @Override
    Vec3f getPivot(AircraftEntity entity) {
        return new Vec3f(0.0f, 0.2f, 0.0f);
    }
}
