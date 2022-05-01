package immersive_airships.client.render.entity.model;

import immersive_airships.entity.GyrodyneEntity;
import net.minecraft.client.model.ModelPart;

public class GyrodyneEntityModel<T extends GyrodyneEntity> extends AirshipEntityModel<T> {
    public GyrodyneEntityModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setAngles(T entity, float f, float g, float h, float i, float j) {
        super.setAngles(entity, f, g, h, i, j);

        float second = (float)((System.currentTimeMillis() % 1000000) / 1000D);
        propeller.yaw += entity.getEnginePower();
        leftWing.pitch = (float)Math.cos(second) * 0.1f;
        rightWing.pitch = (float)Math.cos(second) * 0.1f;

        controller.roll = entity.pitchVelocity / 50.0f;
        controller.pitch = entity.yawVelocity / 50.0f;
    }
}
