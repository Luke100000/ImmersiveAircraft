package immersive_aircraft.network.s2c;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.data.AircraftDataLoader;
import immersive_aircraft.entity.misc.AircraftData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class AircraftDataMessage extends Message {
    private final Map<ResourceLocation, AircraftData> data;

    public AircraftDataMessage() {
        this.data = AircraftDataLoader.REGISTRY;
    }

    public AircraftDataMessage(FriendlyByteBuf buffer) {
        data = new HashMap<>();

        int dataCount = buffer.readInt();
        for (int i = 0; i < dataCount; i++) {
            ResourceLocation identifier = buffer.readResourceLocation();
            data.put(identifier, new AircraftData(buffer));
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(data.size());

        for (ResourceLocation identifier : data.keySet()) {
            buffer.writeResourceLocation(identifier);
            data.get(identifier).encode(buffer);
        }
    }

    @Override
    public void receive(Player player) {
        AircraftDataLoader.CLIENT_REGISTRY.clear();
        AircraftDataLoader.CLIENT_REGISTRY.putAll(data);
    }
}
