package immersive_aircraft.cobalt.network;

import immersive_aircraft.Main;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public abstract class Message implements CustomPacketPayload {
    protected Message() {

    }

    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createType(String id) {
        return new Type<>(Main.locate(id));
    }

    public abstract void encode(RegistryFriendlyByteBuf b);

    public void receiveServer(ServerPlayer e) {
        Main.LOGGER.warn("Received an unhandled server message: {}", this);
    }

    public void receiveClient() {
        Main.LOGGER.warn("Received an unhandled client message: {}", this);
    }

    @Override
    abstract public Type<? extends CustomPacketPayload> type();
}
