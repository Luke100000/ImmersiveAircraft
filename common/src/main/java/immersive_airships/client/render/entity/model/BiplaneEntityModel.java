package immersive_airships.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import immersive_airships.entity.BiplaneEntity;
import net.minecraft.client.model.*;

public class BiplaneEntityModel<T extends BiplaneEntity> extends AirshipEntityModel<T> {
    private final ModelPart propeller;
    private final ModelPart rudder;
    private final ModelPart stabilizer;
    private final ModelPart banner;

    final ImmutableList<ModelPart> parts;

    public BiplaneEntityModel(ModelPart root) {
        super();

        propeller = root.getChild("propeller");
        rudder = root.getChild("rudder");
        stabilizer = root.getChild("stabilizer");
        banner = root.getChild("banner");

        this.parts = ImmutableList.of(
                root.getChild("body"),
                root.getChild("propeller"),
                root.getChild("rudder"),
                root.getChild("stabilizer")
        );
    }

    @Override
    public void setAngles(T entity, float f, float g, float h, float i, float j) {
        super.setAngles(entity, f, g, h, i, j);

        propeller.pitch += entity.getEnginePower();

        rudder.yaw = -entity.yawVelocity / 10.0f;
        stabilizer.roll = -entity.pitchVelocity / 8.0f;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        modelPartData.addChild("banner", ModelPartBuilder.create()
                .uv(0, 0).cuboid(-10.0f, -20.0f, -2.0f, 20.0f, 40.0f, 1.0f),
                ModelTransform.pivot(0, -20, 0));

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

    @Override
    public Iterable<ModelPart> getParts() {
        return parts;
    }

    public ModelPart getBanner() {
        return banner;
    }
}
