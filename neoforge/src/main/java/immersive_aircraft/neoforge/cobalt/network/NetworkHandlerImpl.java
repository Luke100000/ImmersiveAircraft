package immersive_aircraft.neoforge.cobalt.network;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.cobalt.network.NetworkHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.LinkedList;
import java.util.List;

public class NetworkHandlerImpl extends NetworkHandler.Impl {
    record MessageRegistryEntry(CustomPacketPayload.Type type,
                                StreamCodec codec,
                                DirectionalPayloadHandler payloadHandler) {
    }

    List<MessageRegistryEntry> messageRegistry = new LinkedList<>();

    @Override
    public <T extends Message> void registerMessage(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, NetworkHandler.ClientHandler<T> clientHandler, NetworkHandler.ServerHandler<T> serverHandler) {
        DirectionalPayloadHandler<T> payloadHandler = new DirectionalPayloadHandler<>(
                (m, c) -> clientHandler.handle(m),
                (m, c) -> serverHandler.handle(m, (ServerPlayer) c.player())
        );
        messageRegistry.add(new MessageRegistryEntry(type, codec, payloadHandler));
    }

    @Override
    public void sendToServer(Message m) {
        PacketDistributor.sendToServer(m);
    }

    @Override
    public void sendToPlayer(Message m, ServerPlayer e) {
        PacketDistributor.sendToPlayer(e, m);
    }

    public void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        messageRegistry.forEach(entry -> registrar.playBidirectional(
                entry.type,
                entry.codec,
                entry.payloadHandler
        ));
    }
}
