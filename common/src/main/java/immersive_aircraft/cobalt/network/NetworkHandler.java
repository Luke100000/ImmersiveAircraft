package immersive_aircraft.cobalt.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Function;

public abstract class NetworkHandler {
    private static Impl INSTANCE;

    public static <T extends Message> void registerMessage(Class<T> msg, Function<FriendlyByteBuf, T> constructor) {
        INSTANCE.registerMessage(msg, constructor);
    }

    public static void sendToServer(Message m) {
        INSTANCE.sendToServer(m);
    }

    public static void sendToPlayer(Message m, ServerPlayer e) {
        INSTANCE.sendToPlayer(m, e);
    }

    public abstract static class Impl {
        protected Impl() {
            INSTANCE = this;
        }

        public abstract <T extends Message> void registerMessage(Class<T> msg, Function<FriendlyByteBuf, T> constructor);

        public abstract void sendToServer(Message m);

        public abstract void sendToPlayer(Message m, ServerPlayer e);
    }
}
