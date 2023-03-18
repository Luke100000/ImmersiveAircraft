package immersive_aircraft.item.upgrade;

public enum AircraftStat {
    STRENGTH(true),
    FRICTION(true),
    ACCELERATION(true),
    DURABILITY(true),
    FUEL(false),
    WIND(false);

    private final boolean positive;

    AircraftStat(boolean positive) {
        this.positive = positive;
    }

    public boolean isPositive() {
        return positive;
    }
}
