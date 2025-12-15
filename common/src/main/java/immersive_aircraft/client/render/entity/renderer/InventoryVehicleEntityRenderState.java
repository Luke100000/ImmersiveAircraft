package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.entity.weapon.Weapon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class InventoryVehicleEntityRenderState extends DyeableVehicleEntityRenderState {
    public final List<Weapon> weapons;
    public final List<ItemStack> banners;
    public final List<ItemStack> sailDyes;

    public InventoryVehicleEntityRenderState() {
        super();
        this.weapons = new ArrayList<>();
        this.banners = new ArrayList<>();
        this.sailDyes = new ArrayList<>();
    }
}
