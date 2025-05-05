package immersive_aircraft.resources.bbmodel;

public interface BBFaceContainer {
    String getName();

    int getColor();

    Iterable<BBFace> getFaces();

    default boolean enableCulling() {
        return false;
    }
}
