package immersive_aircraft;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

/**
 * 1.12.2 port: the 1.16 build let the mod loader pick up {@code mixins.immersive_aircraft.json}
 * on its own. LaunchWrapper needs Mixin to be booted from a core plugin instead, before any of
 * the targeted vanilla classes are loaded.
 * <p>
 * In a production install the manifest ({@code FMLCorePlugin}) points FML here. In a dev run,
 * where the mod sits on the classpath rather than in {@code mods/}, add
 * {@code -Dfml.coreMods.load=immersive_aircraft.MixinLoader} to the JVM arguments.
 */
@IFMLLoadingPlugin.Name(Main.MOD_ID)
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions("immersive_aircraft.mixin")
public class MixinLoader implements IFMLLoadingPlugin {
    public MixinLoader() {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.immersive_aircraft.json");
        MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
