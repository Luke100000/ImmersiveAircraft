package immersive_aircraft.cobalt.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public abstract class Message {
    protected Message() {

    }

    public abstract void encode(FriendlyByteBuf b);

    public abstract void receive(Player e);
}
