package immersive_aircraft.entity.misc;

import immersive_aircraft.compat.Vector4f;
import immersive_aircraft.config.Config;

public class Trail {
    public final float[] buffer;
    public final int size;
    public final float gray;
    public int lastIndex;
    public int entries;
    public int nullEntries;

    public Trail(int length) {
        this(length, 1.0f);
    }

    public Trail(int length, float gray) {
        buffer = new float[7 * length];
        size = length;
        this.gray = gray;
    }

    public void add(Vector4f first, Vector4f second, float alpha) {
        if (!Config.getInstance().enableTrails) {
            return;
        }

        if (alpha <= 0.0) {
            nullEntries++;
        } else {
            nullEntries = 0;
        }

        if (nullEntries < size) {
            int i = lastIndex * 7;
            buffer[i] = first.getX();
            buffer[i + 1] = first.getY();
            buffer[i + 2] = first.getZ();
            buffer[i + 3] = second.getX();
            buffer[i + 4] = second.getY();
            buffer[i + 5] = second.getZ();
            buffer[i + 6] = alpha;
        }

        lastIndex = (lastIndex + 1) % size;
        entries++;
    }
}
