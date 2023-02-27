package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.entity.AircraftEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class CommandMessage implements Message {
    private final Key key;
    private final double fx;
    private final double fy;
    private final double fz;

    public CommandMessage(Key key, Vec3d velocity) {
        this.key = key;
        this.fx = velocity.x;
        this.fy = velocity.y;
        this.fz = velocity.z;
    }

    @Override
    public void receive(PlayerEntity e) {
        if (e.getRootVehicle() instanceof AircraftEntity) {
            if (key == Key.DISMOUNT) {
                AircraftEntity aircraft = (AircraftEntity) e.getRootVehicle();
                e.stopRiding();
                aircraft.chill();
                aircraft.setVelocity(fx, fy, fz);
            }
        }
    }

    public enum Key {
        DISMOUNT
    }
}
