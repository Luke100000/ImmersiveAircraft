package immersive_aircraft.network.s2c;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.data.VehicleDataLoader;
import immersive_aircraft.entity.misc.VehicleData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class AircraftDataMessage extends Message {
    public static final StreamCodec<RegistryFriendlyByteBuf, AircraftDataMessage> STREAM_CODEC = StreamCodec.ofMember(AircraftDataMessage::encode, AircraftDataMessage::new);
    public static final CustomPacketPayload.Type<AircraftDataMessage> TYPE = Message.createType("aircraft_data");

    public CustomPacketPayload.Type<AircraftDataMessage> type() {
        return TYPE;
    }

    private final Map<Identifier, VehicleData> data;

    public AircraftDataMessage() {
        this.data = VehicleDataLoader.REGISTRY;
    }

    public AircraftDataMessage(RegistryFriendlyByteBuf buffer) {
        data = new HashMap<>();

        int dataCount = buffer.readInt();
        for (int i = 0; i < dataCount; i++) {
            Identifier identifier = buffer.readIdentifier();
            data.put(identifier, new VehicleData(buffer));
        }
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(data.size());

        for (Identifier identifier : data.keySet()) {
            buffer.writeIdentifier(identifier);
            data.get(identifier).encode(buffer);
        }
    }

    @Override
    public void receiveClient() {
        VehicleDataLoader.CLIENT_REGISTRY.clear();
        VehicleDataLoader.CLIENT_REGISTRY.putAll(data);
    }
}
