package immersive_aircraft.fabric.cobalt.network;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.cobalt.network.NetworkHandler;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class NetworkHandlerImpl extends NetworkHandler.Impl {
    private final Map<Class<?>, Identifier> identifiers = new HashMap<>();

    private int id = 0;

    private <T> Identifier createMessageIdentifier(Class<T> msg) {
        return new Identifier(Main.SHORT_MOD_ID, msg.getSimpleName().toLowerCase(Locale.ROOT).substring(0, 8) + id++);
    }

    private Identifier getMessageIdentifier(Message msg) {
        return Objects.requireNonNull(identifiers.get(msg.getClass()), "Used unregistered message!");
    }

    @Override
    public <T extends Message> void registerMessage(Class<T> msg, Function<PacketByteBuf, T> constructor) {
        Identifier identifier = createMessageIdentifier(msg);
        identifiers.put(msg, identifier);

        ServerPlayNetworking.registerGlobalReceiver(identifier, (server, player, handler, buffer, responder) -> {
            Message m = constructor.apply(buffer);
            server.execute(() -> m.receive(player));
        });

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientProxy.register(identifier, constructor);
        }
    }

    @Override
    public void sendToServer(Message msg) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        msg.encode(buf);
        ClientPlayNetworking.send(getMessageIdentifier(msg), buf);
    }

    @Override
    public void sendToPlayer(Message msg, ServerPlayerEntity e) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        msg.encode(buf);
        ServerPlayNetworking.send(e, getMessageIdentifier(msg), buf);
    }

    // Fabric's APIs are not side-agnostic.
    // We punt this to a separate class file to keep it from being eager-loaded on a server environment.
    private static final class ClientProxy {
        private ClientProxy() {
            throw new RuntimeException("new ClientProxy()");
        }

        public static <T extends Message> void register(Identifier id, Function<PacketByteBuf, T> constructor) {
            ClientPlayNetworking.registerGlobalReceiver(id, (client, ignore1, buffer, ignore2) -> {
                Message m = constructor.apply(buffer);
                client.execute(() -> m.receive(client.player));
            });
        }
    }
}

