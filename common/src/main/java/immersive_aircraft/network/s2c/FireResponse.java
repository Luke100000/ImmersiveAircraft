package immersive_aircraft.network.s2c;

import org.joml.Vector3f;
import org.joml.Vector4f;
import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class FireResponse extends Message {
    public final double x, y, z;
    public final double vx, vy, vz;

    public FireResponse(FriendlyByteBuf b) {
        x = b.readDouble();
        y = b.readDouble();
        z = b.readDouble();
        vx = b.readDouble();
        vy = b.readDouble();
        vz = b.readDouble();
    }

    public FireResponse(Vector4f position, Vector3f direction) {
        x = position.x();
        y = position.y();
        z = position.z();
        vx = direction.x();
        vy = direction.y();
        vz = direction.z();
    }

    @Override
    public void encode(FriendlyByteBuf b) {
        b.writeDouble(x);
        b.writeDouble(y);
        b.writeDouble(z);
        b.writeDouble(vx);
        b.writeDouble(vy);
        b.writeDouble(vz);
    }

    @Override
    public void receive(Player e) {
        Main.networkManager.handleFire(this);
    }
}
