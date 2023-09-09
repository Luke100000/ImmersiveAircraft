package immersive_aircraft.combat;

import immersive_aircraft.Main;
import immersive_aircraft.client.gui.VehicleScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
@JeiPlugin
public class JEICCombat implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return Main.locate("extended_storage");
    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        IModPlugin.super.registerGuiHandlers(registration);

        registration.addGuiContainerHandler(VehicleScreen.class, new IGuiContainerHandler<>() {
            @Override
            public @NotNull List<Rect2i> getGuiExtraAreas(@NotNull VehicleScreen containerScreen) {
                final int x = containerScreen.getX();
                final int y = containerScreen.getY();

                return containerScreen.getMenu().getVehicle().getInventoryDescription().getRectangles().stream()
                        .map(r -> new Rect2i(
                                x + r.getX(),
                                y + r.getY(),
                                r.getWidth(),
                                r.getHeight()
                        )).toList();
            }
        });
    }
}
