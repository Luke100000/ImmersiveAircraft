package immersive_aircraft.client.render.entity.renderer;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import immersive_aircraft.Main;
import immersive_aircraft.client.render.entity.MeshRenderer;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.BiplaneEntity;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import immersive_aircraft.util.Utils;
import immersive_aircraft.resources.obj.Mesh;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

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
                            (vertexConsumerProvider, entity, matrixStack, light, tickDelta) -> {
                                List<ItemStack> slots = entity.getSlots(VehicleInventoryDescription.SlotType.BANNER);
                                int i = 0;
                                for (ItemStack slot : slots) {
                                    if (!slot.isEmpty() && slot.getItem() instanceof BannerItem) {
                                        List<Pair<Holder<BannerPattern>, DyeColor>> patterns = Utils.parseBannerItem(slot);
                                        Mesh mesh = MeshRenderer.getFaces(id, "banner_" + (i++));
                                        MeshRenderer.renderBanner(matrixStack, vertexConsumerProvider, light, mesh, true, patterns);
                                    }
                                }
                            }
                    )
            )
            .add(
                    new Object(id, "propeller").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(0.0f, 0.3125f, 0.0f);
                                matrixStack.mulPose(Axis.ZP.rotationDegrees((float) (entity.engineRotation.getSmooth(tickDelta) * 100.0)));
                                matrixStack.translate(0.0f, -0.3125f, 0.0f);
                            }
                    )
            )
            .add(
                    new Object(id, "elevator").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(0.0f, 0.0625f, -2.5f);
                                matrixStack.mulPose(Axis.XP.rotationDegrees(-entity.pressingInterpolatedZ.getSmooth(tickDelta) * 20.0f));
                                matrixStack.translate(0.0f, -0.0625f, 2.5f);
                            }
                    )
            )
            .add(
                    new Object(id, "rudder").setAnimationConsumer(
                            (entity, yaw, tickDelta, matrixStack) -> {
                                matrixStack.translate(0.0f, 0.0625f, -2.5f);
                                matrixStack.mulPose(Axis.YP.rotationDegrees(-entity.pressingInterpolatedX.getSmooth(tickDelta) * 18.0f));
                                matrixStack.translate(0.0f, -0.0625f, 2.5f);
                            }
                    )
            );

    public BiplaneEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
        texture = Main.locate("textures/entity/biplane.png");
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
        return new Vector3f(0.0f, 0.4f, 0.05f);
    }
}
