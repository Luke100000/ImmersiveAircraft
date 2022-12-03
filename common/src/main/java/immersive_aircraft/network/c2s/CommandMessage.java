package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.AircraftEntity;
import net.minecraft.entity.player.PlayerEntity;

public class CommandMessage implements Message {
    private final Key key;

    public CommandMessage(Key key) {
        this.key = key;
    }

    @Override
    public void receive(PlayerEntity e) {
        if (e.getRootVehicle() instanceof AircraftEntity) {
            if (key == Key.USE) {
                e.stopRiding();
            }
        }
    }

    public enum Key {
        USE;
    }
}
