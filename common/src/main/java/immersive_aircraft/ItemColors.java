package immersive_aircraft;

/**
 * Item color registration was removed in 1.21.11.
 * Dye-based item tinting is now handled via item model JSON using tint_sources.
 *
 * For each dyeable vehicle item (WARSHIP, AIRSHIP, CARGO_AIRSHIP), add to the
 * item model JSON:
 *   "tints": [{"type": "minecraft:dye", "default": 15518860}]
 *
 * where 15518860 = 0xECC88C (the previous default color).
 */
public class ItemColors {
    // No-op: tinting is now declarative via item model JSON tint_sources.
}
