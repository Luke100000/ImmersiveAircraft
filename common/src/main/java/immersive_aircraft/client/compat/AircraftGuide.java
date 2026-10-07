package immersive_aircraft.client.compat;

import immersive_aircraft.client.KeyBindings;
import immersive_aircraft.client.AircraftInput;
import immersive_aircraft.entity.AirplaneEntity;
import immersive_aircraft.entity.InventoryVehicleEntity;
import immersive_aircraft.entity.VehicleEntity;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.List;

final class AircraftGuide {
    static final List<KeyMapping> HINT_ORDER = List.of(
            KeyBindings.up, KeyBindings.down, KeyBindings.throttleUp, KeyBindings.throttleDown, KeyBindings.pull, KeyBindings.push,
            KeyBindings.forward, KeyBindings.backward, KeyBindings.left, KeyBindings.right,
            KeyBindings.boost, KeyBindings.use, KeyBindings.dismount);

    static KeyMapping binding(KeyMapping key, Player player) {
        return key == KeyBindings.throttleUp && player.getRootVehicle() instanceof AirplaneEntity ? AircraftInput.throttleKey() : key;
    }

    static String label(KeyMapping key, Player player) {
        if (!(player.getRootVehicle() instanceof VehicleEntity vehicle)) {
            return null;
        }
        if (key == KeyBindings.use) {
            return vehicle instanceof InventoryVehicleEntity inventory && player.getMainHandItem().isEmpty()
                   && inventory.getWeapons().values().stream().flatMap(Collection::stream)
                           .anyMatch(weapon -> vehicle.getGunner(weapon.getGunnerOffset()) == player)
                    ? key.getName() : null;
        }
        if (key != KeyBindings.dismount && vehicle.getControllingPassenger() != player) {
            return null;
        }
        boolean airplane = vehicle instanceof AirplaneEntity;
        if ((key == KeyBindings.push || key == KeyBindings.pull || key == KeyBindings.throttleUp || key == KeyBindings.throttleDown) && !airplane
            || (key == KeyBindings.forward || key == KeyBindings.backward || key == KeyBindings.up || key == KeyBindings.down) && airplane
            || key == KeyBindings.boost && !vehicle.canBoost()) {
            return null;
        }
        return key.getName();
    }
}
