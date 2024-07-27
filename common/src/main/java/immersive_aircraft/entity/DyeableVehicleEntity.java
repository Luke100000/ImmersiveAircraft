package immersive_aircraft.entity;

import immersive_aircraft.client.ColorUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/*
Added functionality to allow vehicles to be dyed.
- Code by Cibernet
 */
public abstract class DyeableVehicleEntity extends VehicleEntity {
    protected static final EntityDataAccessor<Integer> DYE_COLOR = SynchedEntityData.defineId(DyeableVehicleEntity.class, EntityDataSerializers.INT);

    public DyeableVehicleEntity(EntityType<? extends VehicleEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world, canExplodeOnCrash);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        entityData.define(DYE_COLOR, -1);
    }

    @Override
    protected void addItemTag(@NotNull CompoundTag tag) {
        super.addItemTag(tag);

        if (getDyeColor() >= 0) {
            tag.getCompound("display").putInt("color", getDyeColor());
        }
    }

    @Override
    protected void readItemTag(@NotNull CompoundTag tag) {
        super.readItemTag(tag);

        CompoundTag displayTag = tag.getCompound("display");
        if (displayTag.contains("color", 99)) {
            setDyeColor(displayTag.getInt("color"));
        }
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("Color")) {
            setDyeColor(tag.getInt("Color"));
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putInt("Color", getDyeColor());
    }

    public int getDyeColor() {
        return entityData.get(DYE_COLOR);
    }

    public void setDyeColor(int v) {
        entityData.set(DYE_COLOR, v);
    }

    public int getBodyColor() {
        //Gets dye color and separates it into RGB, then turns that into HSB
        int[] rgb = ColorUtils.hexToRGB(getDyeColor() < 0 ? getDefaultDyeColor() : getDyeColor());
        float[] hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);

        //Clamps Brightness value to prevent color from being too dark
        hsb[2] = Mth.clamp(hsb[2], 0.18f, 0.95f);

        //Turns color back into decimal and returns outcome
        Color resultColor = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
        return resultColor.getRGB();
    }

    public int getHighlightColor() {
        //Gets dye color and separates it into RGB, then turns that into HSB
        int[] rgb = ColorUtils.hexToRGB(getBodyColor());
        float[] hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);

        //Multiplies Saturation (hsb[1]) and Brightness (hsb[2]) by a factor
        hsb[1] = Mth.clamp(hsb[1] * 0.88311f, 0, 1);
        hsb[2] = Mth.clamp(hsb[2] * 1.11494f, 0, 1);

        //Turns color back into decimal and returns outcome
        Color resultColor = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
        return resultColor.getRGB();
    }

    public int getDefaultDyeColor() {
        return 0xFFFFFF;
    }

    @Override
    public Component getDisplayName() {
        return super.getDisplayName();
    }
}