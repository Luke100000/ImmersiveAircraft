package immersive_aircraft.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerSynchronizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayer.class)
public interface ServerPlayerEntityMixin {
    @Accessor
    ContainerSynchronizer getContainerSynchronizer();

    @Invoker("nextContainerCounter")
    void ic$nextContainerCounter();

    @Accessor
    int getContainerCounter();
}
