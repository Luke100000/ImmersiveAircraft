package immersive_aircraft.cobalt.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public abstract class NetworkHandler {
    private static Impl INSTANCE;

    public interface ClientHandler<T extends Message> {
        void handle(T message);
    }

    public interface ServerHandler<T extends Message> {
        void handle(T message, ServerPlayer player);
    }

    public static <T extends Message> void handleDefault(T message, ServerPlayer e) {
        message.receiveServer(e);
    }

    public static <T extends Message> void handleDefault(T message) {
        message.receiveClient();
    }

    public static <T extends Message> void registerMessage(String namespace, CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        registerMessage(namespace, type, codec, NetworkHandler::handleDefault, NetworkHandler::handleDefault);
    }

    public static <T extends Message> void registerMessage(String namespace, CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, NetworkHandler.ClientHandler<T> clientHandler, NetworkHandler.ServerHandler<T> serverHandler) {
        INSTANCE.registerMessage(namespace, type, codec, clientHandler, serverHandler);
    }

    public static void sendToServer(Message m) {
        INSTANCE.sendToServer(m);
    }

    public static void sendToPlayer(Message m, ServerPlayer e) {
        INSTANCE.sendToPlayer(m, e);
    }

    public static void sendToTrackingPlayers(Message m, Entity origin) {
        INSTANCE.sendToTrackingPlayers(m, origin);
    }

    public abstract static class Impl {
        protected Impl() {
            INSTANCE = this;
        }

        public abstract <T extends Message> void registerMessage(String namespace, CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, NetworkHandler.ClientHandler<T> clientHandler, NetworkHandler.ServerHandler<T> serverHandler);

        public abstract void sendToServer(Message m);

        public abstract void sendToPlayer(Message m, ServerPlayer e);

        public abstract void sendToTrackingPlayers(Message m, Entity origin);
    }
}
