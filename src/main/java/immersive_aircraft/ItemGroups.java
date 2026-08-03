package immersive_aircraft;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class ItemGroups {
    public static final CreativeTabs GROUP = new CreativeTabs(Main.MOD_ID + "." + Main.MOD_ID + "_tab") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.BIPLANE.get());
        }
    };
}
