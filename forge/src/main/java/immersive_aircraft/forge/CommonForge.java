package immersive_aircraft.forge;

import immersive_aircraft.*;
import immersive_aircraft.data.UpgradeDataLoader;
import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.forge.cobalt.network.NetworkHandlerImpl;
import immersive_aircraft.forge.cobalt.registration.RegistrationImpl;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static net.minecraft.registry.RegistryKeys.ITEM_GROUP;

@Mod(Main.MOD_ID)
@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Bus.MOD)
public final class CommonForge {
    public CommonForge() {
        RegistrationImpl.bootstrap();
        new NetworkHandlerImpl();
        Messages.loadMessages();

        Items.bootstrap();
        Sounds.bootstrap();
        Entities.bootstrap();

        DEF_REG.register(FMLJavaModLoadingContext.get().getModEventBus());
        Registration.registerDataLoader("aircraft_upgrades", new UpgradeDataLoader());
    }

    public static final DeferredRegister<ItemGroup> DEF_REG = DeferredRegister.create(ITEM_GROUP, Main.MOD_ID);

    public static final RegistryObject<ItemGroup> TAB = DEF_REG.register(Main.MOD_ID, () -> ItemGroup.builder()
            .displayName(ItemGroups.getDisplayName())
            .icon(ItemGroups::getIcon)
            .entries((featureFlags, output) -> output.addAll(Items.getSortedItems()))
            .build()
    );
}
