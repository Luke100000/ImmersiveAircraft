package immersive_aircraft.client.render.entity.weaponRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import immersive_aircraft.entity.AircraftEntity;
import immersive_aircraft.entity.weapons.Weapon;
import net.minecraft.client.renderer.MultiBufferSource;

public abstract class WeaponRenderer<W extends Weapon> {
    public abstract <T extends AircraftEntity> void render(T entity, W weapon, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, float tickDelta);
}
