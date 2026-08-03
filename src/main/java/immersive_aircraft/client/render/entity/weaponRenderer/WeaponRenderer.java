package immersive_aircraft.client.render.entity.weaponRenderer;

import immersive_aircraft.compat.Matrix4f;
import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.weapon.Weapon;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public abstract class WeaponRenderer<W extends Weapon> {
    private static final FloatBuffer MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);

    /**
     * Multiplies the current GL matrix by a compat Matrix4f (row-major storage,
     * OpenGL wants column-major).
     */
    public static void glMultMatrix(Matrix4f matrix) {
        float[] row = new float[16];
        MATRIX_BUFFER.clear();
        matrix.writeRowFirst(MATRIX_BUFFER);
        MATRIX_BUFFER.get(row);
        MATRIX_BUFFER.clear();
        for (int c = 0; c < 4; c++) {
            for (int r = 0; r < 4; r++) {
                MATRIX_BUFFER.put(row[r * 4 + c]);
            }
        }
        MATRIX_BUFFER.flip();
        GlStateManager.multMatrix(MATRIX_BUFFER);
    }

    public <T extends VehicleEntity> void render(T entity, W weapon, float tickDelta) {
        GlStateManager.pushMatrix();
        glMultMatrix(weapon.getMount().transform());

        renderModel(entity, weapon, tickDelta);

        GlStateManager.popMatrix();
    }

    protected abstract void renderModel(VehicleEntity entity, W weapon, float tickDelta);
}
