package immersive_aircraft.cobalt.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * 1.12.2 port: implements IMessage. Decoding happens through {@link #decode(PacketBuffer)}
 * (the 1.16 code used a PacketByteBuf constructor instead, but SimpleNetworkWrapper
 * requires a public no-args constructor).
 */
public abstract class Message implements IMessage {
    protected Message() {

    }

    @Override
    public final void fromBytes(ByteBuf buf) {
        decode(new PacketBuffer(buf));
    }

    @Override
    public final void toBytes(ByteBuf buf) {
        encode(new PacketBuffer(buf));
    }

    protected abstract void decode(PacketBuffer b);

    public abstract void encode(PacketBuffer b);

    public abstract void receive(EntityPlayer e);
}
