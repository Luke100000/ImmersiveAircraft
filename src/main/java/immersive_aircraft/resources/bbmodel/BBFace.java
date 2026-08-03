package immersive_aircraft.resources.bbmodel;

public class BBFace {
    public final BBVertex[] vertices;
    public BBTexture texture;

    public BBFace(BBVertex[] vertices) {
        this.vertices = vertices;
    }

    public static class BBVertex {
        public float x, y, z;
        public float nx, ny, nz;
        public float u, v;
    }
}
