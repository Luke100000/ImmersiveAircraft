package immersive_aircraft.util.obj;

public class VertexColor {
    public final float r;
    public final float g;
    public final float b;
    public final float a;

    public VertexColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public String toString() {
        return r + "," + g + "," + b + "," + a;
    }
}