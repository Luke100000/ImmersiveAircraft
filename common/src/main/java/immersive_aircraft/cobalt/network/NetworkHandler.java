package immersive_aircraft.cobalt.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

public abstract class NetworkHandler {
    private static Impl INSTANCE;

    public enum Direction {
        CLIENTBOUND,
        SERVERBOUND
    }

    public static <T extends Message> void registerClientbound(String path, Class<T> msg, Function<FriendlyByteBuf, T> constructor) {
        INSTANCE.registerMessage(path, msg, constructor, Direction.CLIENTBOUND);
    }

    public static <T extends Message> void registerServerbound(String path, Class<T> msg, Function<FriendlyByteBuf, T> constructor) {
        INSTANCE.registerMessage(path, msg, constructor, Direction.SERVERBOUND);
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

        public abstract <T extends Message> void registerMessage(String path, Class<T> msg, Function<FriendlyByteBuf, T> constructor, Direction direction);

        public abstract void sendToServer(Message m);

        public abstract void sendToPlayer(Message m, ServerPlayer e);

        public abstract void sendToTrackingPlayers(Message m, Entity origin);
    }
}
