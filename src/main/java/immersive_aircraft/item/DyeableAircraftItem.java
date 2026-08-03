package immersive_aircraft.item;

/**
 * 1.20.1 parity: marks an aircraft item whose entity can be dyed.
 * The color is transferred from the stack's display.color NBT to the spawned entity
 * (handled in {@link AircraftItem}).
 */
public class DyeableAircraftItem extends AircraftItem {
    public DyeableAircraftItem(AircraftConstructor constructor) {
        super(constructor);
    }
}
