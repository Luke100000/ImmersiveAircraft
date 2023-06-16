package immersive_aircraft.client.render.entity.renderer;

import com.mojang.datafixers.util.Pair;
import immersive_aircraft.Main;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.BiplaneEntity;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.util.Utils;
import immersive_aircraft.util.obj.Mesh;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

import java.util.List;

public class BiplaneEntityRenderer<T extends BiplaneEntity> extends AircraftEntityRenderer<T> {
    private static final Identifier id = Main.locate("objects/biplane.obj");

    private final Identifier texture;

    private final Model model = new Model()
            .add(
                    new Object(id, "frame")
            )
            .add(
                    new Object(id, "banners").setRenderConsumer(
                            (vertexConsumerProvider, entity, matrixStack, light, tickDelta) -> {
                                List<ItemStack> slots = entity.getSlots(VehicleInventoryDescription.SlotType.BANNER);
                                int i = 0;
                                for (ItemStack slot : slots) {
                                    if (!slot.isEmpty() && slot.getItem() instanceof BannerItem) {
                                        List<Pair<RegistryEntry<BannerPattern>, DyeColor>> patterns = Utils.parseBannerItem(slot);
                                        Mesh mesh = getFaces(id, "banner_" + (i++));
                                        renderBanner(matrixStack, vertexConsumerProvider, light, mesh, true, patterns);
                                    }
                                }
                            }
                    )
            )
            .add(
                    new Object(id, "propeller").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(0.0f, 0.3125f, 0.0f);
                                matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(entity.engineRotation.getSmooth(tickDelta) * 100.0)));
                                matrixStack.translate(0.0f, -0.3125f, 0.0f);
                            }
                    )
            )
            .add(
                    new Object(id, "elevator").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(0.0f, 0.0625f, -2.5f);
                                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-entity.pressingInterpolatedZ.getSmooth(tickDelta) * 20.0f));
                                matrixStack.translate(0.0f, -0.0625f, 2.5f);
                            }
                    )
            )
            .add(
                    new Object(id, "rudder").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(0.0f, 0.0625f, -2.5f);
                                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.pressingInterpolatedX.getSmooth(tickDelta) * 18.0f));
                                matrixStack.translate(0.0f, -0.0625f, 2.5f);
                            }
                    )
            );

    public BiplaneEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
        texture = Main.locate("textures/entity/biplane.png");
    }

    @Override
    public void render(T entity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public Identifier getTexture(T AircraftEntity) {
        return texture;
    }

    @Override
    protected Model getModel(AircraftEntity entity) {
        return model;
    }

    @Override
    Vector3f getPivot(AircraftEntity entity) {
        return new Vector3f(0.0f, 0.4f, 0.05f);
    }
}
