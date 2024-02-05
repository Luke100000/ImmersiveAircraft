package immersive_aircraft.client.render.entity.weaponRenderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.weapons.HeavyCrossbow;
import net.minecraft.resources.ResourceLocation;

public class HeavyCrossbowRenderer extends SimpleWeaponRenderer<HeavyCrossbow> {
    static final ResourceLocation ID = Main.locate("objects/heavy_crossbow.obj");
    static final ResourceLocation TEXTURE = Main.locate("textures/entity/heavy_crossbow.png");

    @Override
    ResourceLocation getModelId() {
        return ID;
    }

    @Override
    ResourceLocation getTexture() {
        return TEXTURE;
    }
}
