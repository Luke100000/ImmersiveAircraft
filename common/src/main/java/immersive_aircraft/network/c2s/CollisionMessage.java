package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.config.Config;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class CollisionMessage extends Message {
    public static final StreamCodec<RegistryFriendlyByteBuf, CollisionMessage> STREAM_CODEC = StreamCodec.ofMember(CollisionMessage::encode, CollisionMessage::new);
    public static final CustomPacketPayload.Type<CollisionMessage> TYPE = Message.createType("collision");

    public CustomPacketPayload.Type<CollisionMessage> type() {
        return TYPE;
    }

    private final float damage;

    public CollisionMessage(float damage) {
        this.damage = damage;
    }

    public CollisionMessage(RegistryFriendlyByteBuf b) {
        damage = b.readFloat();
    }

    @Override
    public void encode(RegistryFriendlyByteBuf b) {
        b.writeFloat(damage);
    }

    @Override
    public void receiveServer(ServerPlayer e) {
        if (e.getRootVehicle() instanceof VehicleEntity vehicle) {
            vehicle.hurt(e.level().damageSources().fall(), damage);
            if (vehicle.isRemoved()) {
                float crashDamage = damage * Config.getInstance().crashDamage;
                if (Config.getInstance().preventKillThroughCrash) {
                    crashDamage = Math.min(crashDamage, e.getHealth() - 1.0f);
                }
                e.hurt(e.level().damageSources().fall(), crashDamage);
            }
        }
    }
}
