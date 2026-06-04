package immersive_aircraft.client.render.entity.renderer.bullet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import immersive_aircraft.entity.bullet.TinyTNT;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

public class TinyTNTRenderer extends EntityRenderer<TinyTNT, TinyTNTRenderer.TinyTNTRenderState> {
    private final BlockModelResolver blockModelResolver;
    private final BlockModelRenderState blockRenderState = new BlockModelRenderState();

    public TinyTNTRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.2f;
        this.blockModelResolver = context.getBlockModelResolver();
    }

    @Override
    public TinyTNTRenderState createRenderState() {
        return new TinyTNTRenderState();
    }

    @Override
    public void extractRenderState(TinyTNT entity, TinyTNTRenderState state, float tickDelta) {
        super.extractRenderState(entity, state, tickDelta);
        state.fuse = entity.getFuse();
        state.tickDelta = tickDelta;
        state.light = getPackedLightCoords(entity, tickDelta);
    }

    @Override
    public void submit(TinyTNTRenderState state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        matrixStack.pushPose();
        matrixStack.translate(0.0, 0.5, 0.0);
        if ((float) state.fuse - state.tickDelta + 1.0f < 10.0f) {
            float f = 1.0f - ((float) state.fuse - state.tickDelta + 1.0f) / 10.0f;
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
        blockRenderState.clear();
        blockModelResolver.update(blockRenderState, Blocks.TNT.defaultBlockState(), BlockDisplayContext.create());
        TntMinecartRenderer.submitWhiteSolidBlock(blockRenderState, matrixStack, collector, state.light, state.fuse / 5 % 2 == 0, -1);
        matrixStack.popPose();
        super.submit(state, matrixStack, collector, cameraState);
    }

    public Identifier getTextureLocation(TinyTNT entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    public static class TinyTNTRenderState extends EntityRenderState {
        public int fuse;
        public float tickDelta;
        public int light;
    }
}
