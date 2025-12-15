package immersive_aircraft.resources.bbmodel;

import com.google.gson.JsonObject;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class BBAnimation {
    public final String uuid;
    public final String name;
    public final String loop;
    public final float length;
    public final int snapping;
    public final Map<String, BBAnimator> animators = new HashMap<>();
    public final int frameCount;

    public BBAnimation(JsonObject element) {
        this.uuid = element.getAsJsonPrimitive("uuid").getAsString();
        this.name = element.getAsJsonPrimitive("name").getAsString();
        this.loop = element.getAsJsonPrimitive("loop").getAsString();
        this.length = element.getAsJsonPrimitive("length").getAsFloat();
        this.snapping = element.getAsJsonPrimitive("snapping").getAsInt();

        this.frameCount = Math.max(1, (int) (length * snapping + 0.5));

        if (element.has("animators")) {
            element.getAsJsonObject("animators").entrySet().forEach(entry -> {
                BBAnimator animator = new BBAnimator(entry.getValue().getAsJsonObject(), this);
                this.animators.put(entry.getKey(), animator);
            });
        }
    }

    public boolean hasAnimator(String uuid) {
        return animators.containsKey(uuid);
    }

    public Vector3f sample(String uuid, BBAnimator.Channel channel, float time, BBAnimationVariables vars) {
        return animators.get(uuid).sample(this, channel, time, vars);
    }

    public int toFrameIndex(float length) {
        int frameIndex = (int) (length * snapping + 0.5);
        return Math.floorMod(frameIndex, frameCount);
    }
}
