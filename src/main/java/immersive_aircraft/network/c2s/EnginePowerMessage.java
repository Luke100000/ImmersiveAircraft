package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.EngineAircraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

public class EnginePowerMessage extends Message {
    private float engineTarget;

    public EnginePowerMessage() {
    }

    public EnginePowerMessage(float engineTarget) {
        this.engineTarget = engineTarget;
    }

    @Override
    protected void decode(PacketBuffer b) {
        engineTarget = b.readFloat();
    }

    @Override
    public void encode(PacketBuffer b) {
        b.writeFloat(engineTarget);
    }

    @Override
    public void receive(EntityPlayer e) {
        if (e.getLowestRidingEntity() instanceof EngineAircraft) {
            EngineAircraft entity = (EngineAircraft) e.getLowestRidingEntity();
            entity.setEngineTarget(engineTarget);
        }
    }
}
