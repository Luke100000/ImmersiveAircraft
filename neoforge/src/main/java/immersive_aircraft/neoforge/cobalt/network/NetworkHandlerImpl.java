package immersive_aircraft.neoforge.cobalt.network;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.cobalt.network.NetworkHandler.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class NetworkHandlerImpl extends NetworkHandler.Impl {
    private static final String PROTOCOL_VERSION = "1";

    private final Map<Class<?>, CustomPacketPayload.Type<CobaltPayload>> types = new HashMap<>();
    private final List<MessageRegistration<?>> registrations = new ArrayList<>();

    public NetworkHandlerImpl(IEventBus modEventBus) {
        modEventBus.addListener(this::registerPayloads);
    }

    public void registerClientPayloads(IEventBus modEventBus) {
        ClientProxy.registerPayloads(modEventBus, registrations);
    }

    private Identifier createMessageIdentifier(String path) {
        return Identifier.fromNamespaceAndPath(immersive_aircraft.Main.MOD_ID, path);
    }

    private CustomPacketPayload.Type<CobaltPayload> getMessageType(Message msg) {
        return Objects.requireNonNull(types.get(msg.getClass()), "Used unregistered message!");
    }

    private CobaltPayload createPayload(Message msg) {
        return new CobaltPayload(getMessageType(msg), msg);
    }

    @Override
    public <T extends Message> void registerMessage(String path, Class<T> msg, Function<RegistryFriendlyByteBuf, T> constructor, Direction direction) {
        CustomPacketPayload.Type<CobaltPayload> type = new CustomPacketPayload.Type<>(createMessageIdentifier(path));
        types.put(msg, type);
        registrations.add(new MessageRegistration<>(type, constructor, direction));
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrations.forEach(registration -> registration.register(registrar));
    }

    @Override
    public void sendToServer(Message m) {
        ClientProxy.sendToServer(createPayload(m));
    }

    @Override
    public void sendToPlayer(Message m, ServerPlayer e) {
        PacketDistributor.sendToPlayer(e, createPayload(m));
    }

    @Override
    public void sendToTrackingPlayers(Message m, Entity origin) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(origin, createPayload(m));
    }

    private record MessageRegistration<T extends Message>(
            CustomPacketPayload.Type<CobaltPayload> type,
            Function<RegistryFriendlyByteBuf, T> constructor,
            Direction direction
    ) {
        private void register(PayloadRegistrar registrar) {
            StreamCodec<RegistryFriendlyByteBuf, CobaltPayload> codec = CobaltPayload.codec(type, constructor);
            if (direction == Direction.CLIENTBOUND) {
                registrar.playToClient(type, codec);
            } else {
                registrar.playToServer(type, codec, this::handle);
            }
        }

        private void handle(CobaltPayload payload, IPayloadContext context) {
            payload.message().receive(context.player());
        }
    }

    private static final class ClientProxy {
        private ClientProxy() {
            throw new RuntimeException("new ClientProxy()");
        }

        private static void sendToServer(CobaltPayload payload) {
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(payload);
        }

        private static void registerPayloads(IEventBus modEventBus, List<MessageRegistration<?>> registrations) {
            modEventBus.addListener((net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent event) ->
                    registrations.stream()
                            .filter(registration -> registration.direction() == Direction.CLIENTBOUND)
                            .forEach(registration -> register(event, registration))
            );
        }

        private static <T extends Message> void register(
                net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent event,
                MessageRegistration<T> registration
        ) {
            event.register(registration.type(), registration::handle);
        }
    }

    private record CobaltPayload(CustomPacketPayload.Type<CobaltPayload> type, Message message) implements CustomPacketPayload {
        private static <T extends Message> StreamCodec<RegistryFriendlyByteBuf, CobaltPayload> codec(
                CustomPacketPayload.Type<CobaltPayload> type, Function<RegistryFriendlyByteBuf, T> constructor
        ) {
            return CustomPacketPayload.codec(
                    (payload, buffer) -> payload.message().encode(buffer),
                    buffer -> new CobaltPayload(type, constructor.apply(buffer))
            );
        }
    }
}
