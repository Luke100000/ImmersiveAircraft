package immersive_aircraft.client.render.entity.weaponRenderer;

import immersive_aircraft.client.render.entity.renderer.AircraftEntityRenderer;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.weapon.RotaryCannon;
import immersive_aircraft.entity.weapon.RotationalManager;
import immersive_aircraft.entity.weapon.Telescope;
import immersive_aircraft.entity.weapon.Weapon;
import immersive_aircraft.resources.BBModelBridge;
import immersive_aircraft.util.obj.Mesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Renders a bbmodel weapon model at the weapon's mount transform.
 * If the model has a yaw/pitch/roll bone chain (rotary_cannon, telescope) and the
 * weapon has a RotationalManager, the bones are rotated procedurally, mirroring the
 * 1.20.1 animation variables (yaw -> -y rot, pitch -> +x rot, roll -> -z rot).
 */
public class SimpleWeaponRenderer extends WeaponRenderer<Weapon> {
    private final ResourceLocation id;

    public SimpleWeaponRenderer(ResourceLocation id) {
        this.id = id;
    }

    @Override
    protected void renderModel(VehicleEntity entity, Weapon weapon, float tickDelta) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(BBModelBridge.getTexture(id, 0));
        GlStateManager.disableLighting();

        RotationalManager manager = null;
        if (weapon instanceof RotaryCannon) {
            manager = ((RotaryCannon) weapon).getRotationalManager();
        } else if (weapon instanceof Telescope) {
            manager = ((Telescope) weapon).getRotationalManager();
        }

        if (manager == null || BBModelBridge.getPivot(id, "yaw") == null) {
            // fully static model
            for (String key : BBModelBridge.getSubtreeMeshKeys(id, BBModelBridge.ROOT_BONE)) {
                Mesh mesh = BBModelBridge.getMesh(id, key);
                if (mesh != null) {
                    AircraftEntityRenderer.renderObject(mesh, 1.0f, 1.0f, 1.0f, 1.0f);
                }
            }
        } else {
            Set<String> yawKeys = new HashSet<>(BBModelBridge.getSubtreeMeshKeys(id, "yaw"));
            Set<String> pitchKeys = new HashSet<>(BBModelBridge.getSubtreeMeshKeys(id, "pitch"));
            Set<String> rollKeys = new HashSet<>(BBModelBridge.getSubtreeMeshKeys(id, "roll"));

            // static base (everything not in the yaw chain)
            for (String key : BBModelBridge.getSubtreeMeshKeys(id, BBModelBridge.ROOT_BONE)) {
                if (!yawKeys.contains(key)) {
                    AircraftEntityRenderer.renderObject(BBModelBridge.getMesh(id, key), 1.0f, 1.0f, 1.0f, 1.0f);
                }
            }

            float yaw = (float) (manager.getYaw(tickDelta) / Math.PI * 180.0f);
            float pitch = (float) (manager.getPitch(tickDelta) / Math.PI * 180.0f);
            float roll = (float) (manager.getRoll(tickDelta) / Math.PI * 180.0f);

            float[] yawPivot = BBModelBridge.getPivot(id, "yaw");
            float[] pitchPivot = BBModelBridge.getPivot(id, "pitch");
            float[] rollPivot = BBModelBridge.getPivot(id, "roll");

            GlStateManager.pushMatrix();
            rotateAround(yawPivot, -yaw, 0.0f, 1.0f, 0.0f);
            for (String key : yawKeys) {
                if (!pitchKeys.contains(key)) {
                    AircraftEntityRenderer.renderObject(BBModelBridge.getMesh(id, key), 1.0f, 1.0f, 1.0f, 1.0f);
                }
            }

            GlStateManager.pushMatrix();
            rotateAround(pitchPivot, pitch, 1.0f, 0.0f, 0.0f);
            for (String key : pitchKeys) {
                if (!rollKeys.contains(key)) {
                    AircraftEntityRenderer.renderObject(BBModelBridge.getMesh(id, key), 1.0f, 1.0f, 1.0f, 1.0f);
                }
            }

            GlStateManager.pushMatrix();
            rotateAround(rollPivot, -roll, 0.0f, 0.0f, 1.0f);
            for (String key : rollKeys) {
                AircraftEntityRenderer.renderObject(BBModelBridge.getMesh(id, key), 1.0f, 1.0f, 1.0f, 1.0f);
            }
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }

        GlStateManager.enableLighting();
    }

    private void rotateAround(float[] pivot, float degrees, float ax, float ay, float az) {
        if (pivot != null) {
            GlStateManager.translate(pivot[0], pivot[1], pivot[2]);
            GlStateManager.rotate(degrees, ax, ay, az);
            GlStateManager.translate(-pivot[0], -pivot[1], -pivot[2]);
        }
    }
}
