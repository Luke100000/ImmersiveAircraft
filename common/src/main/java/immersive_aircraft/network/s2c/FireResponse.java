package immersive_aircraft.network.s2c;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.Message;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class FireResponse extends Message {
    public static final StreamCodec<RegistryFriendlyByteBuf, FireResponse> STREAM_CODEC = StreamCodec.ofMember(FireResponse::encode, FireResponse::new);
    public static final CustomPacketPayload.Type<FireResponse> TYPE = Message.createType("fire_response");

    public CustomPacketPayload.Type<FireResponse> type() {
        return TYPE;
    }

    public final double x, y, z;
    public final double vx, vy, vz;

    public FireResponse(RegistryFriendlyByteBuf b) {
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
    public void encode(RegistryFriendlyByteBuf b) {
        b.writeDouble(x);
        b.writeDouble(y);
        b.writeDouble(z);
        b.writeDouble(vx);
        b.writeDouble(vy);
        b.writeDouble(vz);
    }

    @Override
    public void receiveClient() {
        Main.messageHandler.handleFire(this);
    }
}
