package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.network.s2c.InventoryUpdateMessage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class InventoryRequest extends Message {
    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryRequest> STREAM_CODEC = StreamCodec.ofMember(InventoryRequest::encode, InventoryRequest::new);
    public static final CustomPacketPayload.Type<InventoryRequest> TYPE = Message.createType("inventory");

    public CustomPacketPayload.Type<InventoryRequest> type() {
        return TYPE;
    }

    private final int vehicleId;

    public InventoryRequest(int id) {
        this.vehicleId = id;
    }

    public InventoryRequest(RegistryFriendlyByteBuf b) {
        vehicleId = b.readInt();
    }

    @Override
    public void encode(RegistryFriendlyByteBuf b) {
        b.writeInt(vehicleId);
    }

    @Override
    public void receiveServer(ServerPlayer e) {
        Entity entity = e.level().getEntity(vehicleId);
        if (entity instanceof InventoryVehicleEntity vehicle) {
            for (int i = 0; i < vehicle.getInventoryDescription().getInventorySize(); i++) {
                ItemStack stack = vehicle.getInventory().getItem(i);
                if (stack.isEmpty()) continue;
                NetworkHandler.sendToPlayer(new InventoryUpdateMessage(entity, i, stack), e);
            }
        }
    }
}
