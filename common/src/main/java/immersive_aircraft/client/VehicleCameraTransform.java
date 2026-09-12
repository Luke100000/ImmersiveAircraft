package immersive_aircraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class VehicleCameraTransform {
    private VehicleCameraTransform() {
    }

    public static void apply(PoseStack poseStack, float partialTicks, Entity entity, VehicleEntity vehicle, float cameraXRot, float cameraYRot) {
        // rotate camera
        if (vehicle.adaptPlayerRotation) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(vehicle.getRoll(partialTicks)));
            poseStack.mulPose(Axis.XP.rotationDegrees(vehicle.getViewXRot(partialTicks)));
        }

        // fetch eye offset
        float eye = entity.getEyeHeight();

        // transform eye offset to match aircraft rotation
        Vector3f offset = new Vector3f(0, -eye, 0);
        Quaternionf quaternion = Axis.XP.rotationDegrees(0.0f);
        quaternion.mul(Axis.YP.rotationDegrees(-vehicle.getViewYRot(partialTicks)));
        quaternion.mul(Axis.XP.rotationDegrees(vehicle.getViewXRot(partialTicks)));
        quaternion.mul(Axis.ZP.rotationDegrees(vehicle.getRoll(partialTicks)));
        offset.rotate(quaternion);

        // apply camera offset
        poseStack.mulPose(Axis.XP.rotationDegrees(cameraXRot));
        poseStack.mulPose(Axis.YP.rotationDegrees(cameraYRot + 180.0f));
        poseStack.translate(offset.x(), offset.y() + eye, offset.z());
        poseStack.mulPose(Axis.YP.rotationDegrees(-cameraYRot - 180.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(-cameraXRot));
    }
}
