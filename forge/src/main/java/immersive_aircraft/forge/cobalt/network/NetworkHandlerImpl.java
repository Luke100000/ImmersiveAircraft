package immersive_aircraft.forge.cobalt.network;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.cobalt.network.NetworkHandler;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.EventNetworkChannel;

import java.util.HashMap;
import java.util.Map;

public class NetworkHandlerImpl extends NetworkHandler.Impl {
    record MessageRegistryEntry(CustomPacketPayload.Type type,
                                StreamCodec codec,
                                EventNetworkChannel channel) {
    }

    private final Map<ResourceLocation, MessageRegistryEntry> CHANNELS = new HashMap<>();


    @Override
    public <T extends Message> void registerMessage(CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, NetworkHandler.ClientHandler<T> clientHandler, NetworkHandler.ServerHandler<T> serverHandler) {
        if (CHANNELS.containsKey(type.id())) {
            EventNetworkChannel channel = ChannelBuilder.named(type.id()).optional().eventNetworkChannel()
                    .addListener(event -> {
                        FriendlyByteBuf payload = event.getPayload();
                        if (payload != null) {
                            if (event.getSource().isClientSide()) {
                                ClientProxy.handle(codec, clientHandler, payload);
                            } else {
                                ServerPlayer player = event.getSource().getSender();
                                if (player == null) {
                                    throw new IllegalStateException("ServerPlayer is null");
                                }
                                RegistryFriendlyByteBuf byteBuf = new RegistryFriendlyByteBuf(event.getPayload(), player.registryAccess());
                                T msg = codec.decode(byteBuf);
                                serverHandler.handle(msg, player);
                            }
                        }
                    });

            CHANNELS.put(type.id(), new MessageRegistryEntry(
                    type,
                    codec,
                    channel
            ));
        }
    }

    @Override
    public void sendToServer(Message m) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        MessageRegistryEntry entry = CHANNELS.get(m.type().id());
        Connection connection = player.connection.getConnection();
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        m.encode(buf);
        entry.channel.send(buf, connection);
    }

    @Override
    public void sendToPlayer(Message m, ServerPlayer e) {
        MessageRegistryEntry entry = CHANNELS.get(m.type().id());
        Connection connection = e.connection.getConnection();
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), e.registryAccess());
        m.encode(buf);
        entry.channel.send(buf, connection);
    }

    private static final class ClientProxy {
        public static <T extends Message> void handle(StreamCodec<RegistryFriendlyByteBuf, T> codec, NetworkHandler.ClientHandler<T> clientHandler, FriendlyByteBuf payload) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                RegistryFriendlyByteBuf byteBuf = new RegistryFriendlyByteBuf(payload, player.registryAccess());
                T msg = codec.decode(byteBuf);
                clientHandler.handle(msg);
            }
        }
    }
}
