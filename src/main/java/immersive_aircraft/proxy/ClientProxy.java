package immersive_aircraft.proxy;

import immersive_aircraft.Items;
import immersive_aircraft.ItemsClient;
import immersive_aircraft.Main;
import immersive_aircraft.Renderer;
import immersive_aircraft.client.KeyBindings;
import immersive_aircraft.network.ClientNetworkManager;
import immersive_aircraft.resources.ObjectLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        Main.networkManager = new ClientNetworkManager();
        KeyBindings.bootstrap();
        Renderer.bootstrap();

        // drop the cached OBJ meshes on resource reload
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(ObjectLoader.INSTANCE);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        ItemsClient.postLoad();

        // dye tint for the warship item icon (layer0 = warship_color)
        net.minecraft.client.renderer.color.ItemColors itemColors = Minecraft.getMinecraft().getItemColors();
        itemColors.registerItemColorHandler((stack, tintIndex) -> {
            if (tintIndex == 0) {
                net.minecraft.nbt.NBTTagCompound display = stack.getSubCompound("display");
                return display != null && display.hasKey("color") ? display.getInteger("color") : 0xECC88C;
            }
            return 0xFFFFFF;
        }, Items.WARSHIP.get());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Override
    public void addScheduledTaskClient(Runnable task) {
        Minecraft.getMinecraft().addScheduledTask(task);
    }

    @Override
    public EntityPlayer getClientPlayer() {
        return Minecraft.getMinecraft().player;
    }
}
