package immersive_aircraft.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.awt.Color;

/*
 * Added functionality to allow vehicles to be dyed.
 * - Code by Cibernet
 *
 * 1.12.2 port notes:
 *  - the 1.20.1 class extends VehicleEntity; here it extends AirshipEntity because the
 *    only dyeable vehicle (the Warship) needs the airship physics and Java has no
 *    multiple inheritance. The dye logic itself is hierarchy-independent.
 *  - dyeing works sheep-style: right-clicking the vehicle with a dye sets the color
 *    (1.20.1 instead relies on the vanilla leather-dye item mechanic, which 1.12.2
 *    has no generic hook for).
 */
public abstract class DyeableVehicleEntity extends AirshipEntity {
    protected static final DataParameter<Integer> DYE_COLOR = EntityDataManager.createKey(DyeableVehicleEntity.class, DataSerializers.VARINT);

    public DyeableVehicleEntity(World world) {
        super(world);
    }

    @Override
    protected void entityInit() {
        super.entityInit();

        dataManager.register(DYE_COLOR, -1);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);

        if (nbt.hasKey("Color")) {
            setDyeColor(nbt.getInteger("Color"));
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);

        nbt.setInteger("Color", getDyeColor());
    }

    @Override
    protected ItemStack createDropStack() {
        ItemStack stack = super.createDropStack();
        if (getDyeColor() >= 0) {
            stack.getOrCreateSubCompound("display").setInteger("color", getDyeColor());
        }
        return stack;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (held.getItem() instanceof ItemDye) {
            if (!world.isRemote) {
                setDyeColor(EnumDyeColor.byDyeDamage(held.getMetadata()).getColorValue());
                if (!player.capabilities.isCreativeMode) {
                    held.shrink(1);
                }
            }
            return true;
        }
        return super.processInitialInteract(player, hand);
    }

    public int getDyeColor() {
        return dataManager.get(DYE_COLOR);
    }

    public void setDyeColor(int v) {
        dataManager.set(DYE_COLOR, v);
    }

    public int getBodyColor() {
        //Gets dye color and separates it into RGB, then turns that into HSB
        int[] rgb = hexToRGB(getDyeColor() < 0 ? getDefaultDyeColor() : getDyeColor());
        float[] hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);

        //Clamps Brightness value to prevent color from being too dark
        hsb[2] = MathHelper.clamp(hsb[2], 0.18f, 0.95f);

        //Turns color back into decimal and returns outcome
        Color resultColor = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
        return resultColor.getRGB();
    }

    public int getHighlightColor() {
        //Gets dye color and separates it into RGB, then turns that into HSB
        int[] rgb = hexToRGB(getBodyColor());
        float[] hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);

        //Multiplies Saturation (hsb[1]) and Brightness (hsb[2]) by a factor
        hsb[1] = MathHelper.clamp(hsb[1] * 0.88311f, 0, 1);
        hsb[2] = MathHelper.clamp(hsb[2] * 1.11494f, 0, 1);

        //Turns color back into decimal and returns outcome
        Color resultColor = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
        return resultColor.getRGB();
    }

    public int getDefaultDyeColor() {
        return 0xFFFFFF;
    }

    public static int[] hexToRGB(int color) {
        int r = (color & 0xff0000) >> 16;
        int g = (color & 0x00ff00) >> 8;
        int b = (color & 0x0000ff);

        return new int[]{r, g, b};
    }

    public static float[] hexToDecimalRGB(int color) {
        float r = ((color & 0xff0000) >> 16) / 255f;
        float g = ((color & 0x00ff00) >> 8) / 255f;
        float b = ((color & 0x0000ff)) / 255f;

        return new float[]{r, g, b};
    }
}
