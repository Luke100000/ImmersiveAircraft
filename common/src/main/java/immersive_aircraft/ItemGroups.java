package immersive_aircraft;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemGroups {
    public static Identifier getIdentifier() {
        return Main.locate(Main.MOD_ID + "_tab");
    }

    public static Text getDisplayName() {
        return Text.translatable("itemGroup." + ItemGroups.getIdentifier().toTranslationKey());
    }

    public static ItemStack getIcon() {
        return Items.BIPLANE.get().getDefaultStack();
    }

    public static ItemGroup GROUP;
}
