package immersive_aircraft.forge.cobalt.network;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.cobalt.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class NetworkHandlerImpl extends NetworkHandler.Impl {
    private static final String PROTOCOL_VERSION = "1";

    record ChannelHolder(SimpleChannel channel, AtomicInteger id) {
    }

    private final Map<String, ChannelHolder> holders = new HashMap<>();
    private final Map<Class<?>, SimpleChannel> channels = new HashMap<>();

    @Override
    synchronized public <T extends Message> void registerMessage(String namespace, Class<T> msg, Function<FriendlyByteBuf, T> constructor) {
        holders.computeIfAbsent(namespace, (n) -> new ChannelHolder(NetworkRegistry.newSimpleChannel(
                new ResourceLocation(namespace, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        ), new AtomicInteger(0)));

        ChannelHolder holder = holders.get(namespace);
        channels.put(msg, holder.channel());

        holder.channel().registerMessage(holder.id().getAndIncrement(), msg,
                Message::encode,
                constructor,
                (m, ctx) -> {
                    ctx.get().enqueueWork(() -> {
                        ServerPlayer sender = ctx.get().getSender();
                        m.receive(sender);
                    });
                    ctx.get().setPacketHandled(true);
                });
    }

    @Override
    public void sendToServer(Message m) {
        channels.get(m.getClass()).sendToServer(m);
    }

    @Override
    public void sendToPlayer(Message m, ServerPlayer e) {
        channels.get(m.getClass()).send(PacketDistributor.PLAYER.with(() -> e), m);
    }

    @Override
    public void sendToTrackingPlayers(Message m, Entity origin) {
        channels.get(m.getClass()).send(PacketDistributor.TRACKING_ENTITY.with(() -> origin), m);
    }
}
