package immersive_aircraft.fabric.cobalt.data;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class JsonDataLoaderWrapper implements IdentifiableResourceReloadListener {

    private final ResourceLocation id;
    private final PreparableReloadListener dataLoader;

    public JsonDataLoaderWrapper(ResourceLocation id, PreparableReloadListener dataLoader) {
        this.id = id;
        this.dataLoader = dataLoader;
    }

    @Override
    public ResourceLocation getFabricId() {
        return id;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier synchronizer, ResourceManager manager, ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return dataLoader.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
    }

}
