package immersive_aircraft.client;

import immersive_aircraft.Main;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.AirplaneEntity;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.item.upgrade.VehicleStat;
import immersive_aircraft.mixin.client.CameraAccessorMixin;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class MouseFlight {
    public static final float CROSSHAIR_HEIGHT = 0.33f;

    private static final float STEERING_ANGLE = 20.0f;
    private static final float LOOK_AHEAD_TICKS = 8.0f;
    private static final Identifier NOSE_CURSOR = Main.locate("hud/flight_nose");

    private static AircraftEntity aircraft;
    private static boolean enabled;
    private static boolean freeLooking;
    private static float targetYaw;
    private static float targetPitch;

    public static void tick() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        AircraftEntity current = player != null && player.getRootVehicle() instanceof AircraftEntity vehicle
                                 && vehicle.adaptPlayerRotation && vehicle.getControllingPassenger() == player ? vehicle : null;

        if (current != aircraft) {
            enabled = false;
            freeLooking = false;
            aircraft = current;
        }

        boolean acceptsInput = client.screen == null && client.isWindowActive();
        while (KeyBindings.mouseControl.consumeClick()) {
            if (aircraft == null || !acceptsInput) {
                continue;
            }

            enabled = !enabled;
            freeLooking = false;

            Camera camera = client.gameRenderer.getMainCamera();
            float tickDelta = camera.getCameraEntityPartialTicks(client.getDeltaTracker());
            float yawOffset = aircraft.getViewYRot(tickDelta) - aircraft.getYRot();
            float pitchOffset = client.options.getCameraType().isFirstPerson() ? aircraft.getViewXRot(tickDelta) : 0;
            float viewOffset = getCameraPitchOffset(camera, tickDelta);
            if (client.options.getCameraType().isMirrored()) {
                viewOffset = -viewOffset;
            }
            float sign = enabled ? 1.0f : -1.0f;
            setView(player, player.getYRot() + sign * yawOffset, player.getXRot() + sign * (pitchOffset - viewOffset));

            targetYaw = player.getYRot();
            targetPitch = player.getXRot();

            player.sendOverlayMessage(Component.translatable(enabled ? "immersive_aircraft.mouse_control_enabled" : "immersive_aircraft.mouse_control_disabled", KeyBindings.freeLook.getTranslatedKeyMessage()));
        }

        if (!enabled || !acceptsInput) {
            return;
        }

        boolean looking = AircraftInput.strength(KeyBindings.freeLook) > 0;
        if (freeLooking && !looking) {
            setView(player, targetYaw, targetPitch);
        }

        freeLooking = looking;
        if (!freeLooking) {
            targetYaw = player.getYRot();
            targetPitch = player.getXRot();
        }
    }

    private static void setView(LocalPlayer player, float yaw, float pitch) {
        player.setYRot(yaw);
        player.yRotO = yaw;
        player.setXRot(Mth.clamp(pitch, -90.0f, 90.0f));
        player.xRotO = player.getXRot();
        aircraft.onPassengerTurned(player);
    }

    public static boolean isEnabled(VehicleEntity vehicle) {
        return enabled && aircraft == vehicle;
    }

    public static boolean isPiloting(VehicleEntity vehicle) {
        return aircraft != null && aircraft == vehicle;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static float getCameraPitchOffset(Camera camera, float tickDelta) {
        double fov = ((CameraAccessorMixin) camera).immersiveAircraft$calculateFov(tickDelta);
        return (float) (Math.atan((1.0 - 2.0 * CROSSHAIR_HEIGHT) * Math.tan(fov * Mth.DEG_TO_RAD * 0.5)) * Mth.RAD_TO_DEG);
    }

    public static float yawInput(VehicleEntity vehicle, float manualInput) {
        Minecraft client = Minecraft.getInstance();
        if (!isEnabled(vehicle) || manualInput != 0 || client.screen != null || !client.isWindowActive()) {
            return manualInput;
        }
        float speed = aircraft.getProperties().get(VehicleStat.YAW_SPEED);
        return steeringInput(-Mth.wrapDegrees(targetYaw - vehicle.getYRot()), speed, vehicle.pressingInterpolatedX.getSmooth());
    }

    public static float pitchInput(VehicleEntity vehicle, float manualInput) {
        Minecraft client = Minecraft.getInstance();
        if (!isEnabled(vehicle) || !(vehicle instanceof AirplaneEntity) || vehicle.onGround() || manualInput != 0
            || client.screen != null || !client.isWindowActive()) {
            return manualInput;
        }
        float speed = aircraft.getProperties().get(VehicleStat.PITCH_SPEED);
        return steeringInput(targetPitch - vehicle.getXRot(), speed, vehicle.pressingInterpolatedZ.getSmooth());
    }

    private static float steeringInput(float error, float speed, float input) {
        return Mth.clamp((error - input * speed * LOOK_AHEAD_TICKS) / STEERING_ANGLE, -1.0f, 1.0f);
    }

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (!enabled || aircraft == null || client.screen != null || client.options.hideGui) {
            return;
        }
        Camera camera = client.gameRenderer.getMainCamera();
        float tickDelta = camera.getCameraEntityPartialTicks(client.getDeltaTracker());
        double fov = camera.getFov();
        float focalLength = (float) (graphics.guiHeight() / (2.0 * Math.tan(fov * Mth.DEG_TO_RAD * 0.5)));
        float pitch = aircraft instanceof AirplaneEntity ? aircraft.getViewXRot(tickDelta) : 0;
        drawDirection(graphics, camera, Vec3.directionFromRotation(pitch, aircraft.getViewYRot(tickDelta)), focalLength);
    }

    private static void drawDirection(GuiGraphicsExtractor graphics, Camera camera, Vec3 direction, float focalLength) {
        Vector3f vector = direction.toVector3f();
        float depth = vector.dot(camera.forwardVector());
        if (depth <= 0) {
            return;
        }

        float x = graphics.guiWidth() * 0.5f - vector.dot(camera.leftVector()) * focalLength / depth;
        float y = graphics.guiHeight() * 0.5f - vector.dot(camera.upVector()) * focalLength / depth;
        if (x < 8 || y < 8 || x >= graphics.guiWidth() - 8 || y >= graphics.guiHeight() - 8) {
            return;
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.blitSprite(RenderPipelines.CROSSHAIR, NOSE_CURSOR, -4, -4, 9, 9);
        graphics.pose().popMatrix();
    }
}
