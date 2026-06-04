package immersive_aircraft;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ItemGroups {
    public static Identifier getIdentifier() {
        return Main.locate(Main.MOD_ID + "_tab");
    }

    public static Component getDisplayName() {
        return Component.translatable("itemGroup." + ItemGroups.getIdentifier().toLanguageKey());
    }

    public static ItemStack getIcon() {
        return Items.BIPLANE.get().getDefaultInstance();
    }
}
