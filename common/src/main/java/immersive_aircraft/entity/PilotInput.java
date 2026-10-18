package immersive_aircraft.entity;

public record PilotInput(float x, float y, float z) {
    public static final PilotInput NONE = new PilotInput(0.0f, 0.0f, 0.0f);
}
