package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.misc.Trail;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * 1.12.2 port: rendered inside the entity's GL transform, vertices are emitted in
 * entity-local space (world position - interpolated entity position), blending enabled,
 * culling disabled instead of the 1.16 double-emission anti-culling hack.
 */
public class TrailRenderer {
    private static final ResourceLocation identifier = Main.locate("textures/entity/trail.png");

    public static void render(Trail trail, Entity entity, float partialTicks) {
        if (trail.nullEntries >= trail.size || trail.entries == 0) {
            return;
        }

        double ex = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double ey = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double ez = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

        Minecraft.getMinecraft().getTextureManager().bindTexture(identifier);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        //todo a custom vertex indexing methode would be beneficial here
        for (int i = 1; i < Math.min(trail.entries, trail.size); i++) {
            int pre = ((i + trail.lastIndex - 1) % trail.size) * 7;
            int index = ((i + trail.lastIndex) % trail.size) * 7;

            int a1 = (int)((1.0f - ((float)i) / trail.size * 255) * trail.buffer[pre + 6]);
            int a2 = i == (trail.size - 1) ? 0 : (int)((1.0f - ((float)i + 1) / trail.size * 255) * trail.buffer[index + 6]);

            vertex(trail, buffer, 0, 0, pre, ex, ey, ez, a1);
            vertex(trail, buffer, 0, 1, pre + 3, ex, ey, ez, a1);
            vertex(trail, buffer, 1, 1, index + 3, ex, ey, ez, a2);
            vertex(trail, buffer, 1, 0, index, ex, ey, ez, a2);
        }

        tessellator.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static void vertex(Trail trail, BufferBuilder buffer, float u, float v, int index, double ex, double ey, double ez, float a) {
        buffer
                .pos(trail.buffer[index] - ex, trail.buffer[index + 1] - ey, trail.buffer[index + 2] - ez)
                .tex(u, v)
                .color(trail.gray, trail.gray, trail.gray, a)
                .endVertex();
    }
}
