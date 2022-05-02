package immersive_airships.client.render.entity.model;

import immersive_airships.entity.GyrodyneEntity;
import immersive_airships.util.Utils;
import net.minecraft.client.model.ModelPart;

public class GyrodyneEntityModel<T extends GyrodyneEntity> extends AirshipEntityModel<T> {
    public GyrodyneEntityModel(ModelPart root) {
        super(root);
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
}
