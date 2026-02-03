package net.fabricmc.fabric.api.renderer.v1.model;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

/**
 * Compatibility shim for Fabric API versions where this type was removed.
 */
public interface FabricBakedModel {
    default boolean isVanillaAdapter() {
        return true;
    }

    default void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        // no-op compatibility fallback
    }

    default void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
        // no-op compatibility fallback
    }
}
