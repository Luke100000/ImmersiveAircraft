package immersive_aircraft.client.render.entity.weaponRenderer;

import immersive_aircraft.Main;
import immersive_aircraft.entity.weapons.BombBay;
import net.minecraft.resources.ResourceLocation;

public class BombBayRenderer extends SimpleWeaponRenderer<BombBay> {
    static final ResourceLocation ID = Main.locate("objects/bomb_bay.obj");
    static final ResourceLocation TEXTURE = Main.locate("textures/entity/bomb_bay.png");

    @Override
    ResourceLocation getModelId() {
        return ID;
    }

    @Override
    ResourceLocation getTexture() {
        return TEXTURE;
    }
}
