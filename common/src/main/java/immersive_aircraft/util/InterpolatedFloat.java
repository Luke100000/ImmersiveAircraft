package immersive_aircraft.util;

public class InterpolatedFloat {
    private float value;
    private float last;
    private float valueSmooth;
    private float lastSmooth;

    private final float steps;

    public InterpolatedFloat(float steps) {
        this.steps = 1.0f / steps;
    }

    public InterpolatedFloat() {
        this(5.0f);
    }

    public void update(float n) {
        last = value;
        value = n;

        lastSmooth = valueSmooth;
        valueSmooth = valueSmooth * (1.0f - steps) + n * steps;
    }

    public float get(float tickDelta) {
        return last + tickDelta * (value - last);
    }

    public float getSmooth(float tickDelta) {
        return lastSmooth + tickDelta * (valueSmooth - lastSmooth);
    }

    public float getValue() {
        return value;
    }
}
