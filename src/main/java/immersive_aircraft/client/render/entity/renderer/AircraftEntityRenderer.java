package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.misc.Trail;
import immersive_aircraft.resources.ObjectLoader;
import immersive_aircraft.util.obj.Face;
import immersive_aircraft.util.obj.FaceVertex;
import immersive_aircraft.util.obj.Mesh;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.List;

/**
 * 1.12.2 port notes:
 *  - MatrixStack -> GlStateManager push/translate/rotate/pop; the GL matrix transforms the
 *    raw mesh vertices, so no manual position/normal matrix math is needed
 *  - VertexConsumer -> Tessellator/BufferBuilder
 *  - Lighting: rendered full-bright for now
 *    TODO(port): proper lightmap via entity.getBrightnessForRender() + OpenGlHelper
 *  - Banner pattern rendering is stubbed (see {@link #renderBanner})
 */
public abstract class AircraftEntityRenderer<T extends AircraftEntity> extends Render<T> {
    public interface AnimationConsumer<T> {
        void run(T entity, float yaw, float tickDelta);
    }

    public interface RenderConsumer<T> {
        void run(T entity, float tickDelta);
    }

    protected class Object {
        public Object(ResourceLocation id, String object) {
            this.id = id;
            this.object = object;
        }

        private final ResourceLocation id;
        private final String object;

        private AnimationConsumer<T> animationConsumer = null;
        private RenderConsumer<T> renderConsumer = (entity, tickDelta) -> {
            //Get vertex consumer
            bindTexture(getTexture(entity));
            GlStateManager.enableCull();
            renderObject(getMesh(), 1.0f, 1.0f, 1.0f, 1.0f);
        };

        public Mesh getMesh() {
            Mesh mesh = getFaces(id, object);
            if (mesh == null) {
                throw new RuntimeException(String.format("Mesh %s in %s does not exist!", id, object));
            }
            return mesh;
        }

        public ResourceLocation getId() {
            return id;
        }

        public AnimationConsumer<T> getAnimationConsumer() {
            return animationConsumer;
        }

        public Object setAnimationConsumer(AnimationConsumer<T> animationConsumer) {
            this.animationConsumer = animationConsumer;
            return this;
        }

        public RenderConsumer<T> getRenderConsumer() {
            return renderConsumer;
        }

        public Object setRenderConsumer(RenderConsumer<T> renderConsumer) {
            this.renderConsumer = renderConsumer;
            return this;
        }
    }

    protected class Model {

        public Model() {
        }

        private final List<Object> objects = new LinkedList<>();

        public Model add(Object o) {
            objects.add(o);
            return this;
        }

        public List<Object> getObjects() {
            return objects;
        }
    }

    public AircraftEntityRenderer(RenderManager renderManager) {
        super(renderManager);
    }

    protected abstract Model getModel(AircraftEntity entity);

    protected abstract immersive_aircraft.compat.Vec3f getPivot(AircraftEntity entity);

    protected abstract ResourceLocation getTexture(T entity);

    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return getTexture(entity);
    }

    /**
     * Renders the mounted weapons (1.20.1 InventoryVehicleRenderer logic): blocking mounts
     * are hidden in first person when the local player rides the vehicle.
     */
    protected void renderWeapons(T entity, float partialTicks) {
        net.minecraft.client.entity.EntityPlayerSP player = net.minecraft.client.Minecraft.getMinecraft().player;
        for (java.util.List<immersive_aircraft.entity.weapon.Weapon> weapons : entity.getWeapons().values()) {
            for (immersive_aircraft.entity.weapon.Weapon weapon : weapons) {
                if (!weapon.getMount().blocking() || net.minecraft.client.Minecraft.getMinecraft().gameSettings.thirdPersonView != 0 || player == null || !entity.isPassenger(player)) {
                    immersive_aircraft.client.render.entity.weaponRenderer.WeaponRenderer<immersive_aircraft.entity.weapon.Weapon> renderer = immersive_aircraft.WeaponRendererRegistry.get(weapon);
                    if (renderer != null) {
                        renderer.render(entity, weapon, partialTicks);
                    }
                }
            }
        }
    }

    @Override
    public void doRender(T entity, double x, double y, double z, float yaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);

        // TODO(port): full-bright rendering; wire proper lightmap coordinates
        GlStateManager.disableLighting();

        // render trails (in the un-rotated, entity-relative space, like the 1.16 code
        // which captured the matrix stack entry before pushing)
        for (Trail trail : entity.getTrails()) {
            TrailRenderer.render(trail, entity, partialTicks);
        }

        //Wobble
        float h = (float) entity.getDamageWobbleTicks() - partialTicks;
        float j = entity.getDamageWobbleStrength() - partialTicks;
        if (j < 0.0f) {
            j = 0.0f;
        }
        if (h > 0.0f) {
            GlStateManager.rotate(MathHelper.sin(h) * h * j / 10.0f * (float) entity.getDamageWobbleSide(), 1.0f, 0.0f, 0.0f);
        }

        immersive_aircraft.compat.Vec3f effect = entity.onGround ? new immersive_aircraft.compat.Vec3f() : entity.getWindEffect();
        float pitch = entity.prevRotationPitch + (entity.getPitch() - entity.prevRotationPitch) * partialTicks;
        GlStateManager.rotate(-yaw, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(pitch + effect.getZ(), 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(entity.getRoll(partialTicks) + effect.getX(), 0.0f, 0.0f, 1.0f);

        immersive_aircraft.compat.Vec3f pivot = getPivot(entity);
        GlStateManager.translate(pivot.getX(), pivot.getY(), pivot.getZ());

        //Render parts
        Model model = getModel(entity);
        for (Object object : model.getObjects()) {
            if (object.getAnimationConsumer() != null) {
                GlStateManager.pushMatrix();
                object.getAnimationConsumer().run(entity, yaw, partialTicks);
            }
            object.getRenderConsumer().run(entity, partialTicks);
            if (object.getAnimationConsumer() != null) {
                GlStateManager.popMatrix();
            }
        }

        //Render weapons
        renderWeapons(entity, partialTicks);

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();

        super.doRender(entity, x, y, z, yaw, partialTicks);
    }

    public static void renderObject(Mesh mesh, float r, float g, float b, float a) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        for (Face face : mesh.faces) {
            if (face.vertices.size() == 4) {
                for (FaceVertex v : face.vertices) {
                    buffer
                            .pos(v.v.x, v.v.y, v.v.z)
                            .tex(v.t.u, v.t.v)
                            .color(r, g, b, a)
                            .normal(v.n.x, v.n.y, v.n.z)
                            .endVertex();
                }
            }
        }
        tessellator.draw();
    }

    protected static void renderSailObject(Mesh mesh, double time, float r, float g, float b, float a) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        for (Face face : mesh.faces) {
            if (face.vertices.size() == 4) {
                for (FaceVertex v : face.vertices) {
                    double angle = v.v.x + v.v.z + v.v.y * 0.25 + time * 0.25;
                    double scale = 0.05;
                    float x = (float) (v.v.x + (Math.cos(angle) + Math.cos(angle * 1.7)) * scale * v.c.r);
                    float z = (float) (v.v.z + (Math.sin(angle) + Math.sin(angle * 1.7)) * scale * v.c.r);
                    buffer
                            .pos(x, v.v.y, z)
                            .tex(v.t.u, v.t.v)
                            .color(r, g, b, a)
                            .normal(v.n.x, v.n.y, v.n.z)
                            .endVertex();
                }
            }
        }
        tessellator.draw();
    }

    /**
     * Sail animation formula from the 1.20.1 BBModelRenderer (distance-scaled wave),
     * used by the bbmodel-based renderers (warship net/flags).
     */
    protected static void renderSailObjectBB(Mesh mesh, double time, float distanceScale, float baseScale, float r, float g, float b, float a) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        for (Face face : mesh.faces) {
            if (face.vertices.size() == 4) {
                for (FaceVertex v : face.vertices) {
                    float distance = Math.max(
                            Math.max(
                                    Math.abs(v.v.x),
                                    Math.abs(v.v.y)
                            ),
                            Math.abs(v.v.z)
                    );
                    double angle = (v.v.x + v.v.z + v.v.y * 0.25) * 4.0f + time * 4.0f;
                    double scale = distanceScale * distance + baseScale;
                    float x = (float) ((Math.cos(angle) + Math.cos(angle * 1.7)) * scale);
                    float z = (float) ((Math.sin(angle) + Math.sin(angle * 1.7)) * scale);
                    buffer
                            .pos(v.v.x + x, v.v.y, v.v.z + z)
                            .tex(v.t.u, v.t.v)
                            .color(r, g, b, a)
                            .normal(v.n.x, v.n.y, v.n.z)
                            .endVertex();
                }
            }
        }
        tessellator.draw();
    }

    static void renderBanner(Mesh mesh, List<?> patterns) {
        // TODO(port): banner pattern rendering. 1.16 sampled the banner pattern atlas
        //  (SpriteIdentifier); 1.12.2 would need TileEntityBannerRenderer-style texture
        //  binding. Skipped for now - banners simply do not render.
    }

    protected static Mesh getFaces(ResourceLocation id, String object) {
        return ObjectLoader.getObject(id, object);
    }

    @Override
    public boolean shouldRender(T entity, ICamera camera, double camX, double camY, double camZ) {
        if (!entity.isInRangeToRender3d(camX, camY, camZ)) {
            return false;
        }
        AxisAlignedBB box = entity.getRenderBoundingBox().grow(2.5);
        return camera.isBoundingBoxInFrustum(box);
    }
}
