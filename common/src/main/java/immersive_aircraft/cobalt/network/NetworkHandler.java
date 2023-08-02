package immersive_aircraft.cobalt.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Function;

public abstract class NetworkHandler {
    private static Impl INSTANCE;

    public static <T extends Message> void registerMessage(Class<T> msg, Function<PacketByteBuf, T> constructor) {
        INSTANCE.registerMessage(msg, constructor);
    }

    public static void sendToServer(Message m) {
        INSTANCE.sendToServer(m);
    }

    public static void sendToPlayer(Message m, ServerPlayerEntity e) {
        INSTANCE.sendToPlayer(m, e);
    }

    public abstract static class Impl {
        protected Impl() {
            INSTANCE = this;
        }

        public abstract <T extends Message> void registerMessage(Class<T> msg, Function<PacketByteBuf, T> constructor);

        public abstract void sendToServer(Message m);

        public abstract void sendToPlayer(Message m, ServerPlayerEntity e);
    }
}
