package immersive_aircraft.client.render.entity.renderer.bullet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import immersive_aircraft.entity.bullet.TinyTNT;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
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
    public void extractRenderState(TinyTNT entity, TinyTNTRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.fuse = entity.getFuse();
        state.partialTicks = tickDelta;
    }

    @Override
    public void submit(TinyTNTRenderState state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        matrixStack.pushPose();
        matrixStack.translate(0.0, 0.5, 0.0);
        int i = state.fuse;
        if ((float)i - state.partialTicks + 1.0f < 10.0f) {
            float f = 1.0f - ((float)i - state.partialTicks + 1.0f) / 10.0f;
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
        TntMinecartRenderer.submitWhiteSolidBlock(Blocks.TNT.defaultBlockState(), matrixStack, collector, state.lightCoords, i / 5 % 2 == 0, state.outlineColor);
        matrixStack.popPose();
        super.submit(state, matrixStack, collector, cameraState);
    }
}
