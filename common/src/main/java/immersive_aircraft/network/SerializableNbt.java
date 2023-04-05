package immersive_aircraft.network;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class SerializableNbt implements Serializable {
    private static final long serialVersionUID = 5728742776742369248L;

    transient NbtCompound nbt;

    public SerializableNbt(NbtCompound nbt) {
        this.nbt = nbt;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        NbtIo.write(nbt, out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        nbt = NbtIo.read(in);
    }

    public NbtCompound getNbt() {
        return nbt;
    }
}
