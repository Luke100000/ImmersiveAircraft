package owens.oobjloader;

public class VertexColor {
    public float r;
    public float g;
    public float b;
    public float a;

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