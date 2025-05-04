package immersive_aircraft.fabric.cobalt.network;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.cobalt.network.NetworkHandler;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class NetworkHandlerImpl extends NetworkHandler.Impl {
    @Override
    public <T extends Message> void registerMessage(String namespace, CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, NetworkHandler.ClientHandler<T> clientHandler, NetworkHandler.ServerHandler<T> serverHandler) {
        if (clientHandler != null) PayloadTypeRegistry.playS2C().register(type, codec);
        if (serverHandler != null) PayloadTypeRegistry.playC2S().register(type, codec);

        if (clientHandler != null && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientProxy.register(type, clientHandler);
        }

        if (serverHandler != null) {
            ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> serverHandler.handle(payload, context.player()));
        }
    }

    @Override
    public void sendToServer(Message msg) {
        ClientProxy.sendToServer(msg);
    }

    @Override
    public void sendToPlayer(Message msg, ServerPlayer e) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), e.registryAccess());
        msg.encode(buf);
        ServerPlayNetworking.send(e, msg);
    }

    @Override
    public void sendToTrackingPlayers(Message msg, Entity e) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), e.registryAccess());
        msg.encode(buf);
        for (ServerPlayer player : PlayerLookup.tracking(e)) {
            ServerPlayNetworking.send(player, msg);
        }
    }

    // Prevent eager loading client side code
    private static final class ClientProxy {
        private ClientProxy() {
            // Nop
        }

        public static <T extends Message> void register(CustomPacketPayload.Type<T> type, NetworkHandler.ClientHandler<T> handler) {
            ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.handle(payload));
        }

        public static void sendToServer(Message msg) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
                msg.encode(buf);
                ClientPlayNetworking.send(msg);
            }
        }
    }
}


