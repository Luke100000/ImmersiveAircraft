package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.network.s2c.InventoryUpdateMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class RequestInventory extends Message {
    private int vehicleId;

    public RequestInventory() {
    }

    public RequestInventory(int id) {
        this.vehicleId = id;
    }

    @Override
    protected void decode(PacketBuffer b) {
        vehicleId = b.readInt();
    }

    @Override
    public void encode(PacketBuffer b) {
        b.writeInt(vehicleId);
    }

    @Override
    public void receive(EntityPlayer e) {
        Entity entity = e.world.getEntityByID(vehicleId);
        if (entity instanceof InventoryVehicleEntity) {
            InventoryVehicleEntity vehicle = (InventoryVehicleEntity) entity;
            for (int i = 0; i < vehicle.getInventoryDescription().getLastSyncIndex(); i++) {
                ItemStack stack = vehicle.getInventory().getStack(i);
                NetworkHandler.sendToPlayer(new InventoryUpdateMessage(this.vehicleId, i, stack), (EntityPlayerMP)e);
            }
        }
    }
}
