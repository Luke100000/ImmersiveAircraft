package immersive_airships.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import immersive_airships.entity.AirshipEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.CompositeEntityModel;

public class AirshipEntityModel extends CompositeEntityModel<AirshipEntity> {
    private static final String BODY = "body";
    private static final String LEFT_WING = "left_wing";
    private static final String RIGHT_WING = "right_wing";
    private static final String PROPELLER = "propeller";
    private static final String PROPELLER_DISABLED = "propeller_disabled";
    private static final String CONTROLLER = "controller";

    private final ImmutableList<ModelPart> parts;

    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart propeller;
    private final ModelPart propellerDisabled;
    private final ModelPart controller;

    public AirshipEntityModel(ModelPart root) {
        this.parts = ImmutableList.of(
                root.getChild(BODY),
                root.getChild(LEFT_WING),
                root.getChild(RIGHT_WING),
                root.getChild(PROPELLER),
                root.getChild(CONTROLLER)
        );

        leftWing = root.getChild(LEFT_WING);
        rightWing = root.getChild(RIGHT_WING);
        propeller = root.getChild(PROPELLER);
        propellerDisabled = root.getChild(PROPELLER_DISABLED);
        controller = root.getChild(CONTROLLER);
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        modelPartData.addChild(BODY, ModelPartBuilder.create()
                        .uv(5, 27).cuboid(-14.0F, -6.0F, -8.0F, 28.0F, 8.0F, 2.0F)
                        .uv(5, 27).cuboid(-14.0F, -6.0F, 6.0F, 28.0F, 8.0F, 2.0F)
                        .uv(46, 0).cuboid(14.0F, -6.0F, -9.0F, 2.0F, 8.0F, 18.0F)
                        .uv(5, 0).cuboid(-16.0F, -3.0F, -9.0F, 2.0F, 5.0F, 18.0F)
                        .uv(107, 71).cuboid(16.0F, 0.0F, -4.0F, 26.0F, 2.0F, 1.0F)
                        .uv(0, 60).cuboid(-17.0F, 2.0F, -9.0F, 34.0F, 1.0F, 18.0F)
                        .uv(107, 71).cuboid(16.0F, 0.0F, 3.0F, 26.0F, 2.0F, 1.0F)
                        .uv(28, 1).cuboid(34.0F, -2.0F, -2.0F, 6.0F, 6.0F, 4.0F)
                        .uv(69, 6).cuboid(36.5F, 0.5F, -5.0F, 1.0F, 1.0F, 10.0F)
                        .uv(105, 75).cuboid(-25.0F, 0.0F, -10.0F, 40.0F, 2.0F, 2.0F)
                        .uv(105, 75).cuboid(-25.0F, 0.0F, 8.0F, 40.0F, 2.0F, 2.0F)
                        .uv(0, 0).cuboid(-0.5F, -31.0F, -0.5F, 1.0F, 32.0F, 1.0F),
                ModelTransform.of(0, 2f, 0, 0, 0, 0));

        modelPartData.addChild(PROPELLER, ModelPartBuilder.create()
                        .uv(0, 80).cuboid(-24, 0, -24, 48, 0, 48),
                ModelTransform.of(0, -28, 0, 0, 0, 0));

        modelPartData.addChild(PROPELLER_DISABLED, ModelPartBuilder.create()
                        .uv(49, 80).cuboid(-24, 0, -24, 48, 0, 48),
                ModelTransform.of(0, -28, 0, 0, 0, 0));

        modelPartData.addChild(LEFT_WING, ModelPartBuilder.create()
                        .uv(162, 96).cuboid(-4, 0, 0, 16, 0, 32),
                ModelTransform.of(0, 0, 8, 0, 0, 0));

        modelPartData.addChild(RIGHT_WING, ModelPartBuilder.create()
                        .uv(162, 63).cuboid(-4, 0, -32, 16, 0, 32),
                ModelTransform.of(0, 0, -8, 0, 0, 0));

        modelPartData.addChild(CONTROLLER, ModelPartBuilder.create()
                        .uv(50, 2).cuboid(-0.5f, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F),
                ModelTransform.of(10, 2, 0, 0, 0, 0));

        return TexturedModelData.of(modelData, 256, 128);
    }

    @Override
    public void setAngles(AirshipEntity airshipEntity, float f, float g, float h, float i, float j) {
        float second = (float)((System.currentTimeMillis() % 1000000) / 1000D);
        propeller.yaw = second * 16.0f;
        leftWing.pitch = (float)Math.cos(second) * 0.1f;
        rightWing.pitch = (float)Math.cos(second) * 0.1f;
        controller.pitch = (float)Math.cos(second) * 0.1f;
        controller.roll = (float)Math.cos(second * 1.7) * 0.1f;
    }

    public ImmutableList<ModelPart> getParts() {
        return this.parts;
    }
}

