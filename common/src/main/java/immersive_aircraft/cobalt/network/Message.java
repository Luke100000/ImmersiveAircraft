package immersive_aircraft.cobalt.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public abstract class Message {
    protected Message() {

    }

    public abstract void encode(PacketByteBuf b);

    public abstract void receive(PlayerEntity e);
}
