package immersive_aircraft.client.render.entity.renderer.bullet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import immersive_aircraft.entity.bullet.TinyTNT;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class TinyTNTRenderer extends EntityRenderer<TinyTNT, TinyTNTRenderState> {

    public TinyTNTRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.2f;
    }

    @Override
    public @NotNull TinyTNTRenderState createRenderState() {
        return new TinyTNTRenderState();
    }

    @Override
    public void submit(TinyTNTRenderState entity, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.0, 0.5, 0.0);
        int i = entity.fuse;
        float g = entity.scale;
        poseStack.scale(g, g, g);
        poseStack.scale(0.375f, 0.375f, 0.375f);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f));
        poseStack.translate(-0.5, -0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
        TntMinecartRenderer.submitWhiteSolidBlock(Blocks.TNT.defaultBlockState(),
                poseStack,
                submitNodeCollector,
                entity.packedLight,
                i / 5 % 2 == 0,
                ARGB.color(0, 255, 255, 255));
        poseStack.popPose();
        super.submit(entity, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public void extractRenderState(TinyTNT entity, TinyTNTRenderState entityRenderState, float partialTicks) {
        super.extractRenderState(entity, entityRenderState, partialTicks);
        entityRenderState.fuse = entity.getFuse();
        entityRenderState.packedLight = this.getPackedLightCoords(entity, partialTicks);

        int i = entity.getFuse();
        if ((float)i - partialTicks + 1.0f < 10.0f) {
            float f = 1.0f - ((float)i - partialTicks + 1.0f) / 10.0f;
            f = Mth.clamp(f, 0.0f, 1.0f);
            f *= f;
            f *= f;
            entityRenderState.scale = 1.0f + f * 0.3f;
        } else {
            entityRenderState.scale = 1f;
        }
    }
}

