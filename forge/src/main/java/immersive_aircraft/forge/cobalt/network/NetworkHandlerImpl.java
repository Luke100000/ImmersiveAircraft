package immersive_aircraft.forge.cobalt.network;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.cobalt.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

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
        holders.computeIfAbsent(namespace, (n) -> new ChannelHolder(
                ChannelBuilder.named(Identifier.fromNamespaceAndPath(namespace, "main"))
                        .networkProtocolVersion(Integer.parseInt(PROTOCOL_VERSION))
                        .simpleChannel(), new AtomicInteger(0)));

        ChannelHolder holder = holders.get(namespace);
        channels.put(msg, holder.channel());

        holder.channel().messageBuilder(msg, holder.id().getAndIncrement())
                .encoder(Message::encode)
                .decoder(constructor)
                .consumerMainThread((m, ctx) -> {
                    ServerPlayer sender = ctx.getSender();
                    m.receive(sender);
                })
                .add();
    }

    @Override
    public void sendToServer(Message m) {
        channels.get(m.getClass()).send(m, PacketDistributor.SERVER.noArg());
    }

    @Override
    public void sendToPlayer(Message m, ServerPlayer e) {
        channels.get(m.getClass()).send(m, PacketDistributor.PLAYER.with(e));
    }

    @Override
    public void sendToTrackingPlayers(Message m, Entity origin) {
        channels.get(m.getClass()).send(m, PacketDistributor.TRACKING_ENTITY.with(origin));
    }
}
