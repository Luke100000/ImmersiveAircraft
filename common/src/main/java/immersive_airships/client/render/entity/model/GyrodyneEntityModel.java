package immersive_airships.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import immersive_airships.entity.GyrodyneEntity;
import immersive_airships.util.Utils;
import net.minecraft.client.model.*;

public class GyrodyneEntityModel<T extends GyrodyneEntity> extends AirshipEntityModel<T> {
    static final String BODY = "body";
    static final String LEFT_WING = "left_wing";
    static final String RIGHT_WING = "right_wing";
    static final String PROPELLER = "propeller";
    static final String PROPELLER_DISABLED = "propeller_disabled";
    static final String CONTROLLER = "controller";
    static final String AXIS = "axis";

    final ImmutableList<ModelPart> parts;
    final ImmutableList<ModelPart> partsInner;

    final ModelPart leftWing;
    final ModelPart rightWing;
    final ModelPart propeller;
    final ModelPart propellerDisabled;
    final ModelPart controller;

    public GyrodyneEntityModel(ModelPart root) {
        super();

        this.parts = ImmutableList.of(
                root.getChild(BODY),
                root.getChild(LEFT_WING),
                root.getChild(RIGHT_WING),
                root.getChild(PROPELLER),
                root.getChild(CONTROLLER),
                root.getChild(AXIS)
        );

        this.partsInner = ImmutableList.of(
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
                        .uv(105, 75).cuboid(-25.0F, 0.0F, 8.0F, 40.0F, 2.0F, 2.0F),
                ModelTransform.of(0, 2f, 0, 0, 0, 0));

        modelPartData.addChild(AXIS, ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-0.5F, -28.0F, -0.5F, 1.0F, 32.0F, 1.0F),
                ModelTransform.of(0, -2f, 0, 0, 0, 0));

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
    public void setAngles(T entity, float f, float g, float h, float i, float j) {
        super.setAngles(entity, f, g, h, i, j);

        float second = (float)((System.currentTimeMillis() % 1000000) / 1000D);
        float speed = (float)entity.getVelocity().length();
        propeller.yaw += entity.getEnginePower();
        leftWing.pitch = (float)(Utils.cosNoise(second * 10.0f) * speed * 0.005f);
        rightWing.pitch = (float)(Utils.cosNoise(second * 10.0f) * speed * 0.005f);

        controller.roll = entity.pitchVelocity / 5.0f;
        controller.pitch = entity.yawVelocity / 5.0f;
    }

    @Override
    public ImmutableList<ModelPart> getParts() {
        return firstPerson ? this.partsInner : this.parts;
    }
}
