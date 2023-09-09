package immersive_aircraft.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerSynchronizer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayer.class)
public interface ServerPlayerEntityMixin {
    @Accessor
    ContainerSynchronizer getContainerSynchronizer();
}
