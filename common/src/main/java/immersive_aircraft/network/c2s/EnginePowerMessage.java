package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.EngineAircraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class EnginePowerMessage extends Message {
    private final float engineTarget;

    public EnginePowerMessage(float engineTarget) {
        this.engineTarget = engineTarget;
    }

    public EnginePowerMessage(FriendlyByteBuf b) {
        engineTarget = b.readFloat();
    }

    @Override
    public void encode(FriendlyByteBuf b) {
        b.writeFloat(engineTarget);
    }

    @Override
    public void receive(Player e) {
        if (e.getRootVehicle() instanceof EngineAircraft entity) {
            entity.setEngineTarget(engineTarget);
        }
    }
}
