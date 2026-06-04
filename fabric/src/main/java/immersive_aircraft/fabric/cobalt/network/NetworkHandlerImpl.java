package immersive_aircraft.fabric.cobalt.network;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.cobalt.network.NetworkHandler;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class NetworkHandlerImpl extends NetworkHandler.Impl {
    private final Map<Class<?>, CustomPacketPayload.Type<CobaltPayload>> types = new HashMap<>();

    private <T> Identifier createMessageIdentifier(String namespace, Class<T> msg) {
        return Identifier.fromNamespaceAndPath(namespace, msg.getSimpleName().toLowerCase(Locale.ROOT));
    }

    private CustomPacketPayload.Type<CobaltPayload> getMessageType(Message msg) {
        return Objects.requireNonNull(types.get(msg.getClass()), "Used unregistered message!");
    }

    private static FriendlyByteBuf toBuffer(CobaltPayload payload) {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(payload.data()));
    }

    private CobaltPayload createPayload(Message msg) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        msg.encode(buf);
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return new CobaltPayload(getMessageType(msg), data);
    }

    @Override
    public <T extends Message> void registerMessage(String namespace, Class<T> msg, Function<FriendlyByteBuf, T> constructor) {
        Identifier identifier = createMessageIdentifier(namespace, msg);
        CustomPacketPayload.Type<CobaltPayload> type = new CustomPacketPayload.Type<>(identifier);
        types.put(msg, type);

        StreamCodec<RegistryFriendlyByteBuf, CobaltPayload> codec = CobaltPayload.codec(type);
        PayloadTypeRegistry.serverboundPlay().register(type, codec);
        PayloadTypeRegistry.clientboundPlay().register(type, codec);

        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
            Message m = constructor.apply(toBuffer(payload));
            context.server().execute(() -> m.receive(context.player()));
        });

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientProxy.register(type, constructor);
        }
    }

    @Override
    public void sendToServer(Message msg) {
        ClientPlayNetworking.send(createPayload(msg));
    }

    @Override
    public void sendToPlayer(Message msg, ServerPlayer e) {
        ServerPlayNetworking.send(e, createPayload(msg));
    }

    @Override
    public void sendToTrackingPlayers(Message msg, Entity origin) {
        CobaltPayload payload = createPayload(msg);
        for (ServerPlayer player : PlayerLookup.tracking(origin)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    // Fabric's APIs are not side-agnostic.
    // We punt this to a separate class file to keep it from being eager-loaded on a server environment.
    private static final class ClientProxy {
        private ClientProxy() {
            throw new RuntimeException("new ClientProxy()");
        }

        public static <T extends Message> void register(CustomPacketPayload.Type<CobaltPayload> type, Function<FriendlyByteBuf, T> constructor) {
            ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                Message m = constructor.apply(toBuffer(payload));
                context.client().execute(() -> m.receive(context.player()));
            });
        }
    }

    private record CobaltPayload(CustomPacketPayload.Type<CobaltPayload> type, byte[] data) implements CustomPacketPayload {
        private static StreamCodec<RegistryFriendlyByteBuf, CobaltPayload> codec(CustomPacketPayload.Type<CobaltPayload> type) {
            return CustomPacketPayload.codec(
                    (payload, buffer) -> buffer.writeByteArray(payload.data()),
                    buffer -> new CobaltPayload(type, FriendlyByteBuf.readByteArray(buffer))
            );
        }
    }
}

