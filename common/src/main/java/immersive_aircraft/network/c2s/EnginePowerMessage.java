package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.EngineAircraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class EnginePowerMessage extends Message {
    private final float engineTarget;

    public EnginePowerMessage(float engineTarget) {
        this.engineTarget = engineTarget;
    }

    public EnginePowerMessage(PacketByteBuf b) {
        engineTarget = b.readFloat();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeFloat(engineTarget);
    }

    @Override
    public void receive(PlayerEntity e) {
        if (e.getRootVehicle() instanceof EngineAircraft entity) {
            entity.setEngineTarget(engineTarget);
        }
    }
}
