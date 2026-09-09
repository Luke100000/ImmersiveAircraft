package immersive_aircraft.client.render.entity.renderer.bullet;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Render state for tiny TNT entities.
 */
public class TinyTNTRenderState extends EntityRenderState {
    public int fuse;
    public float partialTicks;
    public final BlockModelRenderState blockState = new BlockModelRenderState();
}
