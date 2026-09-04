package immersive_aircraft.network.c2s;

import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.data.VehicleSkinDataLoader;
import immersive_aircraft.entity.InventoryVehicleEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/** Applies a cosmetic after validating the request against server-owned data. */
public class SelectVehicleSkinMessage extends Message {
    private final int vehicleId;
    private final ResourceLocation skinId;

    public SelectVehicleSkinMessage(int vehicleId, ResourceLocation skinId) {
        this.vehicleId = vehicleId;
        this.skinId = skinId;
    }

    public SelectVehicleSkinMessage(FriendlyByteBuf buffer) {
        vehicleId = buffer.readVarInt();
        skinId = buffer.readResourceLocation();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(vehicleId);
        buffer.writeResourceLocation(skinId);
    }

    @Override
    public void receive(Player player) {
        if (!(player.getRootVehicle() instanceof InventoryVehicleEntity vehicle)
                || vehicle.getId() != vehicleId
                || vehicle.getControllingPassenger() != player) {
            return;
        }

        if (VehicleSkinDataLoader.canSelect(player, vehicle.identifier, skinId)) {
            vehicle.setVehicleSkin(skinId);
        }
    }
}
