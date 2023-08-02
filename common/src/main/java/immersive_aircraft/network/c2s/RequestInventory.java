package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.network.s2c.InventoryUpdateMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class RequestInventory extends Message {
    private final int vehicleId;

    public RequestInventory(int id) {
        this.vehicleId = id;
    }
    public RequestInventory(PacketByteBuf b) {
        vehicleId = b.readInt();
    }

    @Override
    public void encode(PacketByteBuf b) {
        b.writeInt(vehicleId);
    }

    @Override
    public void receive(PlayerEntity e) {
        Entity entity = e.world.getEntityById(vehicleId);
        if (entity instanceof InventoryVehicleEntity) {
            InventoryVehicleEntity vehicle = (InventoryVehicleEntity) entity;
            for (int i = 0; i < vehicle.getInventoryDescription().getLastSyncIndex(); i++) {
                ItemStack stack = vehicle.getInventory().getStack(i);
                NetworkHandler.sendToPlayer(new InventoryUpdateMessage(this.vehicleId, i, stack), (ServerPlayerEntity)e);
            }
        }
    }
}
