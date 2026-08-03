package immersive_aircraft.network;

import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.network.s2c.FireResponse;
import immersive_aircraft.network.s2c.InventoryUpdateMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class ClientNetworkManager implements NetworkManager {
    @Override
    public void handleInventoryUpdate(InventoryUpdateMessage message) {
        Minecraft client = Minecraft.getMinecraft();
        if (client.world != null && client.player != null) {
            Entity entity = client.world.getEntityByID(message.getVehicle());
            if (entity instanceof InventoryVehicleEntity) {
                InventoryVehicleEntity vehicle = (InventoryVehicleEntity) entity;
                vehicle.getInventory().setStack(message.getIndex(), message.getStack());
            }
        }
    }

    @Override
    public void handleFire(FireResponse response) {
        Minecraft client = Minecraft.getMinecraft();
        if (client.world != null) {
            // Particles
            java.util.Random random = client.world.rand;
            double r = 0.1;
            for (int t = 0; t < 2; ++t) {
                for (int i = 0; i < 5; ++i) {
                    client.world.spawnParticle(t == 0 ? net.minecraft.util.EnumParticleTypes.FLAME : net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL,
                            response.x, response.y, response.z,
                            response.vx + (random.nextDouble() - 0.5) * r,
                            response.vy + (random.nextDouble() - 0.5) * r,
                            response.vz + (random.nextDouble() - 0.5) * r);
                }
            }
        }
    }
}
