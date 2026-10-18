package immersive_aircraft.entity.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;

public final class VehicleDataSerializers {
    public static final EntityDataSerializer<CompoundTag> SEAT_ASSIGNMENTS = new EntityDataSerializer<>() {
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, CompoundTag> codec() {
            return ByteBufCodecs.COMPOUND_TAG;
        }

        @Override
        public CompoundTag copy(CompoundTag value) {
            return value.copy();
        }
    };

    private VehicleDataSerializers() {
    }
}
