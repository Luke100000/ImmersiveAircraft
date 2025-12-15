package immersive_aircraft.resources.bbmodel;

import java.util.Arrays;
import java.util.List;

public enum AnimationVariableName {
    TIME("time"),
    ENGINE_ROTATION("engine_rotation"),
    PRESSING_INTERPOLATED_X("pressing_interpolated_x"),
    PRESSING_INTERPOLATED_Y("pressing_interpolated_y"),
    PRESSING_INTERPOLATED_Z("pressing_interpolated_z"),
    YAW("yaw"),
    PITCH("pitch"),
    ROLL("roll"),
    VELOCITY_X("velocity_x"),
    VELOCITY_Y("velocity_y"),
    VELOCITY_Z("velocity_z"),
    TURRET_YAW("turret_yaw"),
    TURRET_PITCH("turret_pitch"),
    TURRET_COOLDOWN("turret_cooldown"),
    BALLOON_PITCH("balloon_pitch"),
    BALLOON_ROLL("balloon_roll"),
    CHEST("chest");

    private String name;

    AnimationVariableName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static List<String> getAllNames() {
        return Arrays.stream(AnimationVariableName.values())
                .map(AnimationVariableName::getName)
                .toList();
    }
}
