package immersive_aircraft.resources.bbmodel;

public interface BBFaceContainer {
    Iterable<BBFace> getFaces();

    default boolean enableCulling() {
        return false;
    }
}
