package immersive_aircraft.resources.bbmodel;

import com.google.gson.JsonObject;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BBAnimator {
    final Map<Channel, List<BBKeyframe>> frames = new EnumMap<>(Channel.class);
    final Map<Channel, int[]> frameLookup = new EnumMap<>(Channel.class);

    public BBAnimator(JsonObject element, BBAnimation animation) {
        element.getAsJsonArray("keyframes").forEach(entry -> {
            BBKeyframe frame = new BBKeyframe(entry.getAsJsonObject());
            frames.computeIfAbsent(frame.channel, k -> new LinkedList<>()).add(frame);
        });

        for (int i = 0; i < Channel.values().length; i++) {
            Channel channel = Channel.values()[i];
            List<BBKeyframe> keyframes = frames.computeIfAbsent(channel, k -> new LinkedList<>());

            frameLookup.put(channel, new int[animation.frameCount]);
            keyframes.sort((a, b) -> Float.compare(a.time, b.time));

            if (keyframes.isEmpty()) {
                continue;
            }

            int lastLookupIndex = 0;
            int frameIndex = 0;
            for (BBKeyframe keyframe : keyframes) {
                int lookupIndex = animation.toFrameIndex(keyframe.time);
                for (int j = lastLookupIndex; j < lookupIndex; j++) {
                    frameLookup.get(channel)[j] = frameIndex;
                }
                lastLookupIndex = lookupIndex;
                frameIndex++;
            }
        }
    }

    public Vector3f sample(BBAnimation animation, Channel channel, float time, BBAnimationVariables vars) {
        List<BBKeyframe> keyframes = frames.get(channel);
        if (keyframes.isEmpty()) {
            if (channel == Channel.SCALE) {
                return new Vector3f(1, 1, 1);
            } else {
                return new Vector3f();
            }
        }

        if (animation.length == 0) {
            time = 0.0f;
        } else {
            time = time % animation.length;
        }

        int frameIndex = animation.toFrameIndex(time);
        int i = frameLookup.get(channel)[frameIndex];
        BBKeyframe first = keyframes.get(i);
        BBKeyframe second = keyframes.get((i + 1) % keyframes.size());

        if (first.time == second.time) {
            return first.evaluate(vars);
        }

        float delta = (time - first.time) / (second.time - first.time);
        Vector3f firstVector = first.evaluate(vars);
        firstVector.mul(1 - delta);
        Vector3f secondVector = second.evaluate(vars);
        secondVector.mul(delta);
        firstVector.add(secondVector);
        return firstVector;
    }

    public enum Channel {
        POSITION, ROTATION, SCALE
    }
}
