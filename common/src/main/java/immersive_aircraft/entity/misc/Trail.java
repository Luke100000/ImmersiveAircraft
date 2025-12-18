package immersive_aircraft.entity.misc;

import immersive_aircraft.config.Config;
import org.joml.Vector4f;

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
            buffer[i] = first.x;
            buffer[i + 1] = first.y;
            buffer[i + 2] = first.z;
            buffer[i + 3] = second.x;
            buffer[i + 4] = second.y;
            buffer[i + 5] = second.z;
            buffer[i + 6] = alpha;
        }

        lastIndex = (lastIndex + 1) % size;
        entries++;
    }

    private void setBuffer(float[] buffer) {
        System.arraycopy(buffer, 0, this.buffer, 0, buffer.length);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Trail clone() {
        Trail newTrail = new Trail(this.size, this.gray);
        newTrail.setBuffer(this.buffer);
        newTrail.lastIndex = this.lastIndex;
        newTrail.entries = this.entries;
        newTrail.nullEntries = this.nullEntries;
        return newTrail;
    }
}
