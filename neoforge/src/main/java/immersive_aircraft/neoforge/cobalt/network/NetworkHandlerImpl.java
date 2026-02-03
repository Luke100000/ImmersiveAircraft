package immersive_aircraft.neoforge.cobalt.network;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.cobalt.network.NetworkHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NetworkHandlerImpl extends NetworkHandler.Impl {
    record MessageRegistryEntry<T extends Message>(CustomPacketPayload.Type<T> type,
                                                   StreamCodec<RegistryFriendlyByteBuf, T> codec,
                                                   IPayloadHandler<T> serverHandler,
                                                   IPayloadHandler<T> clientHandler) {
    }

    Map<String, List<MessageRegistryEntry<? extends Message>>> messageRegistry = new HashMap<>();

    @Override
    public <T extends Message> void registerMessage(String namespace, CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, NetworkHandler.ClientHandler<T> clientHandler, NetworkHandler.ServerHandler<T> serverHandler) {
        messageRegistry.computeIfAbsent(namespace, k -> new LinkedList<>());
        IPayloadHandler<T> serverPayloadHandler = serverHandler == null ? null
                : (payload, context) -> serverHandler.handle(payload, (ServerPlayer) context.player());
        IPayloadHandler<T> clientPayloadHandler = clientHandler == null ? null
                : (payload, context) -> clientHandler.handle(payload);
        messageRegistry.get(namespace).add(new MessageRegistryEntry<>(type, codec, serverPayloadHandler, clientPayloadHandler));
    }

    @Override
    public void sendToServer(Message m) {
        ClientProxy.sendToServer(m);
    }

    @Override
    public void sendToPlayer(Message m, ServerPlayer e) {
        PacketDistributor.sendToPlayer(e, m);
    }

    @Override
    public void sendToTrackingPlayers(Message m, Entity origin) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(origin, m);
    }

    public void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        messageRegistry.values().forEach(channel -> channel.forEach(entry -> registerEntry(registrar, entry)));
    }

    private static <T extends Message> void registerEntry(PayloadRegistrar registrar, MessageRegistryEntry<T> entry) {
        if (entry.serverHandler() != null && entry.clientHandler() != null) {
            registrar.playBidirectional(entry.type(), entry.codec(), entry.serverHandler(), entry.clientHandler());
        } else if (entry.serverHandler() != null) {
            registrar.playToServer(entry.type(), entry.codec(), entry.serverHandler());
        } else if (entry.clientHandler() != null) {
            registrar.playToClient(entry.type(), entry.codec(), entry.clientHandler());
        }
    }

    // Prevent eager loading client-only networking classes on dedicated servers.
    private static final class ClientProxy {
        private ClientProxy() {
            // Nop
        }

        public static void sendToServer(Message msg) {
            ClientPacketDistributor.sendToServer(msg);
        }
    }
}
