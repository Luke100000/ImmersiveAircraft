package immersive_aircraft.entity;

import immersive_aircraft.Items;
import immersive_aircraft.entity.misc.Trail;
import immersive_aircraft.entity.misc.VehicleInventoryDescription;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

import java.util.List;

public class CargoAirshipEntity extends AirshipEntity {
    {
        getProperties().setMass(8.0f);
        getProperties().setEngineSpeed(0.0175f);
        getProperties().setVerticalSpeed(0.02f);
    }

    private static final VehicleInventoryDescription inventoryDescription = new VehicleInventoryDescription()
            .addSlot(VehicleInventoryDescription.SlotType.BOILER, 8 + 9, 8 + 36)
            .addSlot(VehicleInventoryDescription.SlotType.WEAPON, 8 + 18 * 2 + 6, 8 + 6)
            .addSlot(VehicleInventoryDescription.SlotType.WEAPON, 8 + 18 * 2 + 28, 8 + 6)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 8 + 18 * 2 + 6, 8 + 6 + 22)
            .addSlot(VehicleInventoryDescription.SlotType.UPGRADE, 8 + 18 * 2 + 28, 8 + 6 + 22)
            .addSlot(VehicleInventoryDescription.SlotType.BANNER, 8 + 18 * 2 + 6, 8 + 6 + 22 * 2)
            .addSlot(VehicleInventoryDescription.SlotType.DYE, 8 + 18 * 2 + 28, 8 + 6 + 22 * 2)
            .addSlots(VehicleInventoryDescription.SlotType.INVENTORY, 8 + 18 * 5, 8, 4, 4)
            .addBoxedSlots(VehicleInventoryDescription.SlotType.INVENTORY, -8 - 18 * 4, 8, 4, 4)
            .addBoxedSlots(VehicleInventoryDescription.SlotType.INVENTORY, 186, 8, 4, 4)
            .addBoxedSlots(VehicleInventoryDescription.SlotType.INVENTORY, -8 - 18 * 4, 8 + 18 * 4 + 16, 4, 4)
            .addBoxedSlots(VehicleInventoryDescription.SlotType.INVENTORY, 186, 8 + 18 * 4 + 16, 4, 4)
            .build();

    @Override
    public VehicleInventoryDescription getInventoryDescription() {
        return inventoryDescription;
    }

    public CargoAirshipEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected float getBaseFuelConsumption() {
        return 1.25f;
    }

    @Override
    public Item asItem() {
        return Items.CARGO_AIRSHIP.get();
    }

    private final List<Trail> trails = List.of(
            new Trail(15, 0.5f),
            new Trail(11, 0.5f),
            new Trail(11, 0.5f)
    );

    public List<Trail> getTrails() {
        return trails;
    }

    @Override
    protected void addTrails(Matrix4f transform) {
        Matrix4f tr = transform.copy();
        tr.multiplyByTranslation(0.0f, 0.4f, -1.2f);
        tr.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(engineRotation.getSmooth() * 50.0f));
        trail(tr, 0);

        tr = transform.copy();
        tr.multiplyByTranslation(1.15625f, 2.5f, -1.2f);
        tr.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(engineRotation.getSmooth() * 65.0f));
        trail(tr, 1);

        tr = transform.copy();
        tr.multiplyByTranslation(-1.15625f, 2.5f, -1.2f);
        tr.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(-engineRotation.getSmooth() * 65.0f));
        trail(tr, 2);
    }
}
