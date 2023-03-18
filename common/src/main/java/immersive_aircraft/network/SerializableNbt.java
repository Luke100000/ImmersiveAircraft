package immersive_aircraft.network;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

import java.io.*;

public final class SerializableNbt implements Serializable {
    @Serial
    private static final long serialVersionUID = 5728742776742369248L;

    transient NbtCompound nbt;

    public SerializableNbt(NbtCompound nbt) {
        this.nbt = nbt;
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        NbtIo.write(nbt, out);
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        nbt = NbtIo.read(in);
    }

    public NbtCompound getNbt() {
        return nbt;
    }
}
