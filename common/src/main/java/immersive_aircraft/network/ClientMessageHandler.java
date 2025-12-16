package immersive_aircraft.network;

import immersive_aircraft.client.gui.VehicleScreenRegistry;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.network.s2c.FireResponse;
import immersive_aircraft.network.s2c.InventoryUpdateMessage;
import immersive_aircraft.network.s2c.OpenGuiRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;

public class ClientMessageHandler implements MessageHandler {
    public void handleOpenGuiRequest(OpenGuiRequest message) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.player != null) {
            InventoryVehicleEntity vehicle = (InventoryVehicleEntity) client.level.getEntity(message.getVehicle());
            if (vehicle != null) {
                VehicleScreenRegistry.GUI_OPEN_HANDLERS
                        .getOrDefault(vehicle.getClass(), VehicleScreenRegistry.DEFAULT)
                        .handle(vehicle, client.player, message);
            }
        }
    }

    public void handleInventoryUpdate(InventoryUpdateMessage message) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.player != null) {
            InventoryVehicleEntity vehicle = (InventoryVehicleEntity) client.level.getEntity(message.getVehicle());
            if (vehicle != null) {
                vehicle.getInventory().setItem(message.getIndex(), message.getStack());
            }
        }
    }

    public void handleFire(FireResponse fireResponse) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level != null) {
            // Particles
            RandomSource random = level.getRandom();
            double r = 0.1;
            for (int t = 0; t < 2; ++t) {
                for (int i = 0; i < 5; ++i) {
                    level.addParticle(t == 0 ? ParticleTypes.SMALL_FLAME : ParticleTypes.SMOKE,
                            fireResponse.x, fireResponse.y, fireResponse.z,
                            fireResponse.vx + (random.nextDouble() - 0.5) * r,
                            fireResponse.vy + (random.nextDouble() - 0.5) * r,
                            fireResponse.vz + (random.nextDouble() - 0.5) * r
                    );
                }
            }
        }
    }
}
