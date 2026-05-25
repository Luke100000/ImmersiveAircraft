package immersive_aircraft.mixin.client;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;

/**
 * In 1.21.11, renderHitbox was moved to EntityHitboxDebugRenderer.
 * This mixin is kept as a stub to avoid removing it from the mixin config.
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
}
