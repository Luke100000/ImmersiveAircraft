package immersive_aircraft.mixin.client;

import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.network.c2s.SeatChangeMessage;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Unique
    private int immersiveAircraft$seatKeys;

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void immersiveAircraft$selectSeat(long window, int action, KeyEvent event, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        int key = event.key();
        if (window != client.getWindow().handle() || key < GLFW.GLFW_KEY_1 || key > GLFW.GLFW_KEY_9) return;

        int seat = key - GLFW.GLFW_KEY_1;
        int mask = 1 << seat;
        if (action == GLFW.GLFW_RELEASE) {
            immersiveAircraft$seatKeys &= ~mask;
            return;
        }
        if (action == GLFW.GLFW_REPEAT) {
            if ((immersiveAircraft$seatKeys & mask) != 0) {
                ci.cancel();
            }
            return;
        }

        immersiveAircraft$seatKeys &= ~mask;
        if ((event.modifiers() & GLFW.GLFW_MOD_CONTROL) == 0 || !client.isWindowActive() || client.gui.screen() != null
            || client.player == null || !(client.player.getVehicle() instanceof VehicleEntity vehicle)) return;

        immersiveAircraft$seatKeys |= mask;
        if (seat < vehicle.getPassengerSpace()) {
            NetworkHandler.sendToServer(new SeatChangeMessage(vehicle.getId(), seat));
        }
        ci.cancel();
    }
}
