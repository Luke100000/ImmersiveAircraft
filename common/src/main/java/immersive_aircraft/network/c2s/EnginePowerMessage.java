package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.EngineVehicle;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class EnginePowerMessage extends Message {
    public static final StreamCodec<RegistryFriendlyByteBuf, EnginePowerMessage> STREAM_CODEC = StreamCodec.ofMember(EnginePowerMessage::encode, EnginePowerMessage::new);
    public static final CustomPacketPayload.Type<EnginePowerMessage> TYPE = Message.createType("engine_power");

    public CustomPacketPayload.Type<EnginePowerMessage> type() {
        return TYPE;
    }

    private final float engineTarget;

    public EnginePowerMessage(float engineTarget) {
        this.engineTarget = engineTarget;
    }

    public EnginePowerMessage(RegistryFriendlyByteBuf b) {
        engineTarget = b.readFloat();
    }

    @Override
    public void encode(RegistryFriendlyByteBuf b) {
        b.writeFloat(engineTarget);
    }

    @Override
    public void receiveServer(ServerPlayer e) {
        if (e.getRootVehicle() instanceof EngineVehicle entity && entity.hasPassenger(e)) {
            entity.setEngineTarget(engineTarget);
        }
    }
}
