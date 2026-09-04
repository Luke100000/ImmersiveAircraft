package immersive_aircraft.network.s2c;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.data.VehicleSkin;
import immersive_aircraft.data.VehicleSkinDataLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

/** Synchronizes the server's data-pack skin catalogue to a Fabric client. */
public class VehicleSkinDataMessage extends Message {
    private final Map<ResourceLocation, VehicleSkin> skins;

    public VehicleSkinDataMessage() {
        skins = new HashMap<>(VehicleSkinDataLoader.REGISTRY);
    }

    public VehicleSkinDataMessage(FriendlyByteBuf buffer) {
        skins = new HashMap<>();
        int count = buffer.readVarInt();
        for (int i = 0; i < count; i++) {
            VehicleSkin skin = new VehicleSkin(buffer);
            skins.put(skin.id(), skin);
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(skins.size());
        skins.values().forEach(skin -> skin.encode(buffer));
    }

    @Override
    public void receive(Player player) {
        VehicleSkinDataLoader.replaceClientRegistry(skins);
    }
}
