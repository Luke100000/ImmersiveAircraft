package immersive_airships.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import immersive_airships.entity.BiplaneEntity;
import net.minecraft.client.model.*;

public class BiplaneEntityModel<T extends BiplaneEntity> extends AirshipEntityModel<T> {
    private final ModelPart propeller;
    private final ModelPart rudder;
    private final ModelPart stabilizer;

    final ImmutableList<ModelPart> parts;
    final ImmutableList<ModelPart> bannerParts;

    public BiplaneEntityModel(ModelPart root, ModelPart bannerPart) {
        super();

        propeller = root.getChild("propeller");
        rudder = root.getChild("rudder");
        stabilizer = root.getChild("stabilizer");

        this.parts = ImmutableList.of(
                root.getChild("body"),
                root.getChild("propeller"),
                root.getChild("rudder"),
                root.getChild("stabilizer")
        );

        this.bannerParts = ImmutableList.of(
                bannerPart.getChild("banner1"),
                bannerPart.getChild("banner2")
        );
    }

    @Override
    public void setAngles(T entity, float f, float g, float h, float i, float j) {
        super.setAngles(entity, f, g, h, i, j);

        propeller.pitch += entity.getEnginePower();

        rudder.yaw = -entity.yawVelocity / 5.0f;
        stabilizer.roll = -entity.pitchVelocity / 12.0f;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        ModelPartData body = modelPartData.addChild("body", ModelPartBuilder.create()
                .uv(0, 98).cuboid(11.0F, -22.0F, -48.0F, 16.0F, 1.0F, 96.0F)
                .uv(0, 0).cuboid(11.0F, -30.0F, -48.0F, 16.0F, 1.0F, 96.0F)
                .uv(0, 0).cuboid(-5.0F, -21.0F, -8.0F, 16.0F, 1.0F, 16.0F)
                .uv(0, 18).cuboid(-10.0F, -20.0F, -3.0F, 28.0F, 1.0F, 6.0F)
                .uv(47, 45).cuboid(-14.0F, -26.0F, -7.0F, 9.0F, 6.0F, 14.0F)
                .uv(57, 32).cuboid(-12.0F, -29.0F, -4.0F, 7.0F, 3.0F, 8.0F)
                .uv(19, 59).cuboid(19.0F, -21.0F, -8.0F, 5.0F, 5.0F, 2.0F)
                .uv(0, 59).cuboid(19.0F, -21.0F, 6.0F, 5.0F, 5.0F, 2.0F)
                .uv(49, 6).cuboid(-32.0F, -21.0F, -1.0F, 5.0F, 5.0F, 2.0F)
                .uv(0, 59).cuboid(11.0F, -25.0F, -8.0F, 1.0F, 5.0F, 16.0F)
                .uv(0, 46).cuboid(-36.0F, -27.0F, -3.0F, 24.0F, 6.0F, 6.0F)
                .uv(0, 26).cuboid(9.0F, -31.0F, -4.0F, 20.0F, 11.0F, 8.0F)
                .uv(0, 0).cuboid(29.0F, -29.0F, -2.0F, 2.0F, 7.0F, 4.0F)
                .uv(0, 81).cuboid(-5.0F, -25.0F, 7.0F, 16.0F, 4.0F, 1.0F)
                .uv(49, 0).cuboid(-5.0F, -25.0F, -8.0F, 16.0F, 4.0F, 1.0F), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData propeller = modelPartData.addChild("propeller", ModelPartBuilder.create().uv(49, 6).cuboid(0.0F, -2.5F, -10.0F, 0.0F, 5.0F, 20.0F), ModelTransform.pivot(31.1F, -1.5F, 0.0F));

        ModelPartData stabilizer = modelPartData.addChild("stabilizer", ModelPartBuilder.create().uv(129, 0).cuboid(-8.5F, -0.5F, -19.0F, 11.0F, 1.0F, 38.0F), ModelTransform.pivot(-26.5F, -0.5F, 0.0F));

        ModelPartData rudder = modelPartData.addChild("rudder", ModelPartBuilder.create().uv(35, 66).cuboid(-9.0F, -11.0F, -0.5F, 12.0F, 16.0F, 1.0F), ModelTransform.pivot(-29.0F, -3.0F, 0.0F));

        return TexturedModelData.of(modelData, 256, 256);
    }

    public static TexturedModelData getBannerModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        modelPartData.addChild("banner1", ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-6.0f, -12.0f, -0.5f, 12.0f, 22.0f, 1.0f),
                ModelTransform.of(19.0f, -6.0f, 24.0f, (float)Math.PI / 2, 0, 0));

        modelPartData.addChild("banner2", ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-6.0f, -12.0f, -0.5f, 12.0f, 22.0f, 1.0f),
                ModelTransform.of(19.0f, -6.0f, -24.0f, (float)Math.PI / 2, (float)Math.PI, 0));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public Iterable<ModelPart> getParts() {
        return parts;
    }

    @Override
    public Iterable<ModelPart> getBannerParts() {
        return bannerParts;
    }
}
