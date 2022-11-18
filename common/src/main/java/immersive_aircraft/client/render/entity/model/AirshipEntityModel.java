package immersive_aircraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import immersive_aircraft.entity.AirshipEntity;
import net.minecraft.client.model.*;

public class AirshipEntityModel<T extends AirshipEntity> extends AircraftEntityModel<T> {
    final ModelPart propeller;
    final ModelPart controller;

    final ImmutableList<ModelPart> parts;

    public AirshipEntityModel(ModelPart root) {
        super();

        propeller = root.getChild("propeller");
        controller = root.getChild("controller");

        this.parts = ImmutableList.of(
                root.getChild("body"),
                root.getChild("propeller"),
                root.getChild("controller")
        );
    }

    @Override
    public void setAngles(T entity, float f, float g, float h, float i, float j) {
        super.setAngles(entity, f, g, h, i, j);

        propeller.pitch += entity.getEnginePower();

        controller.roll = entity.pitchVelocity / 5.0f;
        controller.pitch = entity.yawVelocity / 5.0f;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        ModelPartData bb_main = modelPartData.addChild("body", ModelPartBuilder.create().uv(5, 27).cuboid(-13.0F, -6.0F, -9.0F, 27.0F, 8.0F, 2.0F)
                .uv(5, 27).cuboid(-13.0F, -6.0F, 7.0F, 27.0F, 8.0F, 2.0F)
                .uv(46, 0).cuboid(14.0F, -6.0F, -9.0F, 2.0F, 8.0F, 18.0F)
                .uv(0, 60).cuboid(-13.0F, 2.0F, -9.0F, 29.0F, 1.0F, 18.0F)
                .uv(0, 60).cuboid(-17.0F, -49.0F, -13.0F, 34.0F, 24.0F, 26.0F)
                .uv(0, 60).cuboid(-18.0F, -48.0F, -12.0F, 1.0F, 22.0F, 24.0F)
                .uv(0, 60).cuboid(-13.0F, -50.0F, -14.0F, 2.0F, 26.0F, 28.0F)
                .uv(0, 60).cuboid(17.0F, -48.0F, -12.0F, 1.0F, 22.0F, 24.0F)
                .uv(0, 60).cuboid(11.0F, -50.0F, -14.0F, 2.0F, 26.0F, 28.0F)
                .uv(0, 60).cuboid(-33.0F, -48.0F, 0.0F, 15.0F, 22.0F, 0.0F)
                .uv(0, 60).cuboid(-11.0F, -55.0F, 0.0F, 43.0F, 25.0F, 0.0F)
                .uv(0, 60).cuboid(-19.0F, -10.0F, -7.0F, 12.0F, 12.0F, 14.0F)
                .uv(46, 0).cuboid(16.0F, -6.0F, -1.0F, 10.0F, 2.0F, 2.0F)
                .uv(46, 0).cuboid(16.0F, -4.0F, 0.0F, 10.0F, 7.0F, 0.0F)
                .uv(0, 60).cuboid(17.0F, -36.0F, -1.0F, 14.0F, 2.0F, 2.0F), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

        ModelPartData rope_r1 = bb_main.addChild("rope_1", ModelPartBuilder.create().uv(0, 0).cuboid(11.5F, -29.0F, -6.5F, 1.0F, 20.0F, 1.0F), ModelTransform.of(-21.0F, 4.0F, 0.0F, 0.1745F, 0.0F, -0.1745F));

        ModelPartData rope_r2 = bb_main.addChild("rope_2", ModelPartBuilder.create().uv(0, 0).cuboid(11.5F, -29.0F, 6.5F, 1.0F, 20.0F, 1.0F), ModelTransform.of(-21.0F, 4.0F, 0.0F, -0.1745F, 0.0F, -0.1745F));

        ModelPartData rope_r3 = bb_main.addChild("rope_3", ModelPartBuilder.create().uv(0, 0).cuboid(11.5F, -29.0F, -6.5F, 1.0F, 20.0F, 1.0F), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.1745F, 0.0F, 0.1745F));

        ModelPartData propeller_r1 = modelPartData.addChild("propeller", ModelPartBuilder.create().uv(0, 60).cuboid(-21.0F, -11.0F, -7.0F, 0.0F, 14.0F, 14.0F), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.0436F, 0.0F, 0.0F));

        ModelPartData rope_r4 = bb_main.addChild("rope_4", ModelPartBuilder.create().uv(0, 0).cuboid(11.5F, -29.0F, 6.5F, 1.0F, 20.0F, 1.0F), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.1745F));

        ModelPartData ruder_r1 = modelPartData.addChild("controller", ModelPartBuilder.create().uv(50, 2).cuboid(11.75F, -6.0F, -0.5F, 1.0F, 8.0F, 1.0F), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0436F, 0.0F));

        return TexturedModelData.of(modelData, 256, 128);
    }

    @Override
    public Iterable<ModelPart> getParts() {
        return parts;
    }
}
