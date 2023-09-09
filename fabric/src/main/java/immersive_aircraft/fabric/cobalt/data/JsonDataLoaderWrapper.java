package immersive_aircraft.fabric.cobalt.data;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class JsonDataLoaderWrapper implements IdentifiableResourceReloadListener {

	private final Identifier id;
	private final JsonDataLoader dataLoader;

	public JsonDataLoaderWrapper(Identifier id, JsonDataLoader dataLoader) {
		this.id = id;
		this.dataLoader = dataLoader;
	}

	@Override
	public Identifier getFabricId() {
		return id;
	}

	@Override
	public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
		return dataLoader.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
	}

}
