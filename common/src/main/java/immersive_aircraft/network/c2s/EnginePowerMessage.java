package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.EngineAircraft;
import net.minecraft.entity.player.PlayerEntity;

public class EnginePowerMessage implements Message {
    private final float engineTarget;

    public EnginePowerMessage(float engineTarget) {
        this.engineTarget = engineTarget;
    }

    @Override
    public void receive(PlayerEntity e) {
        if (e.getRootVehicle() instanceof EngineAircraft entity) {
            entity.setEngineTarget(engineTarget);
        }
    }
}
