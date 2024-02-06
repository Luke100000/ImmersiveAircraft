package immersive_aircraft.client.render.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import immersive_aircraft.Main;
import immersive_aircraft.WeaponRendererRegistry;
import immersive_aircraft.client.render.entity.BBModelRenderer;
import immersive_aircraft.client.render.entity.MeshRenderer;
import immersive_aircraft.client.render.entity.weaponRenderer.WeaponRenderer;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.weapons.Weapon;
import immersive_aircraft.resources.BBModelLoader;
import immersive_aircraft.resources.bbmodel.BBModel;
import immersive_aircraft.resources.obj.Mesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

import java.util.LinkedList;
import java.util.List;

public abstract class AircraftEntityRenderer<T extends AircraftEntity> extends EntityRenderer<T> {
    protected class Object {
        public interface AnimationConsumer<T> {
            void run(T entity, float yaw, float tickDelta, PoseStack matrixStack);
        }

        public interface RenderConsumer<T> {
            void run(MultiBufferSource vertexConsumerProvider, T entity, PoseStack matrixStack, int light, float tickDelta);
        }

        public Object(ResourceLocation id, String object) {
            this.id = id;
            this.object = object;
        }

        private final ResourceLocation id;
        private final String object;

        private AnimationConsumer<T> animationConsumer = null;
        private RenderConsumer<T> renderConsumer = (vertexConsumerProvider, entity, matrixStack, light, tickDelta) -> {
            //Get vertex consumer
            ResourceLocation identifier = getTextureLocation(entity);
            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.entityCutout(identifier));
            float health = entity.getHealth() * 0.5f + 0.5f;
            MeshRenderer.renderObject(getMesh(), matrixStack, vertexConsumer, light, health, health, health, 1.0f);
        };

        public Mesh getMesh() {
            Mesh mesh = MeshRenderer.getFaces(id, object);
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
            // nop
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

    public AircraftEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    protected abstract Model getModel(AircraftEntity entity);

    protected abstract Vector3f getPivot(AircraftEntity entity);


    @Override
    public void render(T entity, float yaw, float tickDelta, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light) {
        PoseStack.Pose peek = matrixStack.last();

        matrixStack.pushPose();

        //Wobble
        float h = (float) entity.getDamageWobbleTicks() - tickDelta;
        float j = entity.getDamageWobbleStrength() - tickDelta;
        if (j < 0.0f) {
            j = 0.0f;
        }
        if (h > 0.0f) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(Mth.sin(h) * h * j / 10.0f * (float) entity.getDamageWobbleSide()));
        }

        Vector3f effect = entity.isOnGround() ? Vector3f.ZERO : entity.getWindEffect();
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(-yaw));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(entity.getViewXRot(tickDelta) + effect.z()));
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(entity.getRoll(tickDelta) + effect.x()));

        Vector3f pivot = getPivot(entity);
        matrixStack.translate(pivot.x(), pivot.y(), pivot.z());

        //Render parts
        Model model = getModel(entity);
        for (Object object : model.getObjects()) {
            if (object.getAnimationConsumer() != null) {
                matrixStack.pushPose();
                object.getAnimationConsumer().run(entity, yaw, tickDelta, matrixStack);
            }
            object.getRenderConsumer().run(vertexConsumerProvider, entity, matrixStack, light, tickDelta);
            if (object.getAnimationConsumer() != null) {
                matrixStack.popPose();
            }
        }

        // Render model
        BBModel bbModel = BBModelLoader.MODELS.get(Main.locate("gyrodyne"));
        float time = (entity.getLevel().getGameTime() % 24000 + tickDelta) / 20.0f;
        BBModelRenderer.renderModel(bbModel, matrixStack, vertexConsumerProvider, light, time);

        //Render weapons
        LocalPlayer player = Minecraft.getInstance().player;
        for (List<Weapon> weapons : entity.getWeapons().values()) {
            for (Weapon weapon : weapons) {
                if (!weapon.getMount().blocking() || !Main.firstPersonGetter.isFirstPerson() || player == null || !entity.hasPassenger(player)) {
                    WeaponRenderer<Weapon> renderer = WeaponRendererRegistry.get(weapon);
                    if (renderer != null) {
                        renderer.render(entity, weapon, matrixStack, vertexConsumerProvider, light, tickDelta);
                    }
                }
            }
        }

        //Render trails
        entity.getTrails().forEach(t -> TrailRenderer.render(t, vertexConsumerProvider, peek));

        matrixStack.popPose();

        super.render(entity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
    }

    @Override
    public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        if (!entity.shouldRender(x, y, z)) {
            return false;
        }
        AABB box = entity.getBoundingBoxForCulling().inflate(2.5);
        return frustum.isVisible(box);
    }
}

