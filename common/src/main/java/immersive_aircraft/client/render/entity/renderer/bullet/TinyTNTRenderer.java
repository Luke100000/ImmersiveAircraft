package immersive_aircraft.client.render.entity.renderer.bullet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import immersive_aircraft.entity.bullet.TinyTNT;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

public class TinyTNTRenderer extends EntityRenderer<TinyTNT> {
    private final BlockRenderDispatcher blockRenderer;

    public TinyTNTRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.2f;
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(TinyTNT entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();
        matrixStack.translate(0.0, 0.5, 0.0);
        int i = entity.getFuse();
        if ((float)i - partialTicks + 1.0f < 10.0f) {
            float f = 1.0f - ((float)i - partialTicks + 1.0f) / 10.0f;
            f = Mth.clamp(f, 0.0f, 1.0f);
            f *= f;
            f *= f;
            float g = 1.0f + f * 0.3f;
            matrixStack.scale(g, g, g);
        }
        matrixStack.scale(0.375f, 0.375f, 0.375f);
        matrixStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
        matrixStack.translate(-0.5, -0.5, 0.5);
        matrixStack.mulPose(Axis.YP.rotationDegrees(90.0f));
        TntMinecartRenderer.renderWhiteSolidBlock(this.blockRenderer, Blocks.TNT.defaultBlockState(), matrixStack, buffer, packedLight, i / 5 % 2 == 0);
        matrixStack.popPose();
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TinyTNT entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

