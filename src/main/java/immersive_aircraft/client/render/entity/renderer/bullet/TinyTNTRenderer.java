package immersive_aircraft.client.render.entity.renderer.bullet;

import immersive_aircraft.entity.bullet.TinyTNT;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * TinyTNT renderer, following vanilla RenderTNTPrimed with an extra 0.375 scale.
 */
public class TinyTNTRenderer extends Render<TinyTNT> {
    public TinyTNTRenderer(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.2f;
    }

    @Override
    public void doRender(TinyTNT entity, double x, double y, double z, float entityYaw, float partialTicks) {
        BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + 0.5f, (float) z);
        int i = entity.getFuse();
        if ((float) i - partialTicks + 1.0f < 10.0f) {
            float f = 1.0f - ((float) i - partialTicks + 1.0f) / 10.0f;
            f = MathHelper.clamp(f, 0.0f, 1.0f);
            f *= f;
            f *= f;
            float g = 1.0f + f * 0.3f;
            GlStateManager.scale(g, g, g);
        }
        float f2 = (1.0f - ((float) i - partialTicks + 1.0f) / 100.0f) * 0.8f;
        GlStateManager.scale(0.375f, 0.375f, 0.375f);
        GlStateManager.rotate(-90.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.translate(-0.5, -0.5, 0.5);
        GlStateManager.rotate(90.0f, 0.0f, 1.0f, 0.0f);
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        if (i / 5 % 2 == 0) {
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
            GlStateManager.color(1.0f, 1.0f, 1.0f, f2);
            GlStateManager.doPolygonOffset(-3.0f, -3.0f);
            GlStateManager.enablePolygonOffset();
            blockRenderer.renderBlockBrightness(Blocks.TNT.getDefaultState(), 1.0f);
            GlStateManager.doPolygonOffset(0.0f, 0.0f);
            GlStateManager.disablePolygonOffset();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
        } else {
            blockRenderer.renderBlockBrightness(Blocks.TNT.getDefaultState(), entity.getBrightness());
        }
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(TinyTNT entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
