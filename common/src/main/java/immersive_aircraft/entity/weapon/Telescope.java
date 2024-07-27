package immersive_aircraft.entity.weapon;

import immersive_aircraft.entity.VehicleEntity;
import immersive_aircraft.entity.misc.WeaponMount;
import immersive_aircraft.resources.bbmodel.BBAnimationVariables;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public class Telescope extends Weapon {
    private final RotationalManager rotationalManager = new RotationalManager(this);

    int lastFireTick = 0;

    public Telescope(VehicleEntity entity, ItemStack stack, WeaponMount mount, int slot) {
        super(entity, stack, mount, slot);
    }

    @Override
    public void tick() {
        rotationalManager.tick();
        rotationalManager.pointTo(getEntity());

        lastFireTick--;
        if (lastFireTick == 0) {
            Entity pilot = getEntity().getControllingPassenger();
            if (pilot != null) {
                pilot.playSound(SoundEvents.SPYGLASS_STOP_USING, 1.0f, 1.0f);
            }
        }
    }

    @Override
    public void fire(Vector3f direction) {
        // pass
    }

    @Override
    public void clientFire(int index) {
        Entity pilot = getEntity().getControllingPassenger();
        assert pilot != null;
        if (lastFireTick <= 0) {
            pilot.playSound(SoundEvents.SPYGLASS_USE, 1.0f, 1.0f);
        }
        lastFireTick = 2;
    }

    public Boolean isScoping() {
        return lastFireTick > 0;
    }

    @Override
    public <T extends VehicleEntity> void setAnimationVariables(T entity, float time) {
        super.setAnimationVariables(entity, time);

        float tickDelta = time % 1.0f;
        BBAnimationVariables.set("pitch", (float) (rotationalManager.getPitch(tickDelta) / Math.PI * 180.0f));
        BBAnimationVariables.set("yaw", (float) (rotationalManager.getYaw(tickDelta) / Math.PI * 180.0f));
        BBAnimationVariables.set("roll", (float) (rotationalManager.getRoll(tickDelta) / Math.PI * 180.0f));
    }
}
