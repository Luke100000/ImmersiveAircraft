package immersive_aircraft.network.s2c;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.Message;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;

public class FireResponse extends Message {
    public double x, y, z;
    public double vx, vy, vz;

    public FireResponse() {
    }

    public FireResponse(double x, double y, double z, double vx, double vy, double vz) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
    }

    @Override
    protected void decode(PacketBuffer b) {
        x = b.readDouble();
        y = b.readDouble();
        z = b.readDouble();
        vx = b.readDouble();
        vy = b.readDouble();
        vz = b.readDouble();
    }

    @Override
    public void encode(PacketBuffer b) {
        b.writeDouble(x);
        b.writeDouble(y);
        b.writeDouble(z);
        b.writeDouble(vx);
        b.writeDouble(vy);
        b.writeDouble(vz);
    }

    @Override
    public void receive(EntityPlayer e) {
        Main.networkManager.handleFire(this);
    }
}
