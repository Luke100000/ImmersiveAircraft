package immersive_aircraft.network.s2c;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.data.VehicleSkinDataLoader;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OpenGuiRequest extends Message {
    private final int vehicle;
    private final int syncId;
    private final List<ResourceLocation> availableSkins;
    @Nullable
    private final ResourceLocation selectedSkin;

    public OpenGuiRequest(VehicleEntity vehicle, int syncId) {
        this.vehicle = vehicle.getId();
        this.syncId = syncId;
        this.availableSkins = List.of();
        this.selectedSkin = null;
    }

    public OpenGuiRequest(InventoryVehicleEntity vehicle, int syncId, ServerPlayer player) {
        this.vehicle = vehicle.getId();
        this.syncId = syncId;
        ResourceLocation storedSkin = vehicle.getVehicleSkin();
        boolean canCustomize = vehicle.getControllingPassenger() == player;
        if (canCustomize && storedSkin != null && !VehicleSkinDataLoader.canSelect(player, vehicle.identifier, storedSkin)) {
            vehicle.setVehicleSkin(null);
            storedSkin = null;
        }
        this.availableSkins = canCustomize
                ? VehicleSkinDataLoader.getAvailableSkinIds(player, vehicle.identifier)
                : List.of();
        this.selectedSkin = VehicleSkinDataLoader.getActiveServerSkinId(vehicle.identifier, storedSkin).orElse(null);
    }

    public OpenGuiRequest(FriendlyByteBuf b) {
        vehicle = b.readInt();
        syncId = b.readInt();
        int skinCount = b.readVarInt();
        availableSkins = new ArrayList<>(skinCount);
        for (int i = 0; i < skinCount; i++) {
            availableSkins.add(b.readResourceLocation());
        }
        selectedSkin = b.readBoolean() ? b.readResourceLocation() : null;
    }

    @Override
    public void encode(FriendlyByteBuf b) {
        b.writeInt(vehicle);
        b.writeInt(syncId);
        b.writeVarInt(availableSkins.size());
        availableSkins.forEach(b::writeResourceLocation);
        b.writeBoolean(selectedSkin != null);
        if (selectedSkin != null) {
            b.writeResourceLocation(selectedSkin);
        }
    }

    @Override
    public void receive(Player e) {
        Main.networkManager.handleOpenGuiRequest(this);
    }

    public int getVehicle() {
        return vehicle;
    }

    public int getSyncId() {
        return syncId;
    }

    public List<ResourceLocation> getAvailableSkins() {
        return availableSkins;
    }

    @Nullable
    public ResourceLocation getSelectedSkin() {
        return selectedSkin;
    }
}
