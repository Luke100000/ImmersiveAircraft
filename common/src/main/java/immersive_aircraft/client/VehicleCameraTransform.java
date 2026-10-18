package immersive_aircraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.PassengerPositionDescriptor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class VehicleCameraTransform {
    private VehicleCameraTransform() {
    }

    public static void apply(PoseStack poseStack, float partialTicks, Entity entity, VehicleEntity vehicle, float cameraXRot, float cameraYRot) {
        // rotate camera
        if (vehicle.adaptPlayerRotation && !MouseFlight.isEnabled(vehicle)) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(vehicle.getRoll(partialTicks)));
            poseStack.mulPose(Axis.XP.rotationDegrees(vehicle.getViewXRot(partialTicks)));
        }

        // fetch eye offset
        float eye = entity.getEyeHeight();

        Vector3f offset = new Vector3f(0.0f, eye, 0.0f);
        PassengerPositionDescriptor passengerPosition = vehicle.getPassengerPosition(entity);
        if (entity instanceof Player && passengerPosition != null) {
            Quaternionf seatRotation = new Quaternionf().rotationYXZ(
                    -passengerPosition.yaw(), passengerPosition.pitch(), passengerPosition.roll()
            );
            Vector3f seatOffset = new Vector3f(
                    passengerPosition.cameraOffsetX(),
                    passengerPosition.cameraOffsetY(),
                    passengerPosition.cameraOffsetZ()
            );
            offset.add(seatOffset.rotate(seatRotation));
        }

        // Transform the camera offset into the vehicle's axes.
        Quaternionf quaternion = Axis.XP.rotationDegrees(0.0f);
        quaternion.mul(Axis.YP.rotationDegrees(-vehicle.getViewYRot(partialTicks)));
        quaternion.mul(Axis.XP.rotationDegrees(vehicle.getViewXRot(partialTicks)));
        quaternion.mul(Axis.ZP.rotationDegrees(vehicle.getRoll(partialTicks)));
        offset.negate().rotate(quaternion);

        // apply camera offset
        poseStack.mulPose(Axis.XP.rotationDegrees(cameraXRot));
        poseStack.mulPose(Axis.YP.rotationDegrees(cameraYRot + 180.0f));
        poseStack.translate(offset.x(), offset.y() + eye, offset.z());
        poseStack.mulPose(Axis.YP.rotationDegrees(-cameraYRot - 180.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(-cameraXRot));
    }
}
