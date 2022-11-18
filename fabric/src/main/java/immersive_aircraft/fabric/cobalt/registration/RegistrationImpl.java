package immersive_aircraft.fabric.cobalt.registration;

import immersive_aircraft.cobalt.registration.Registration;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

public class RegistrationImpl extends Registration.Impl {
    @Override
    public <T extends Entity> void registerEntityRenderer(EntityType<T> type, EntityRendererFactory<T> constructor) {
        EntityRendererRegistry.register(type, constructor);
    }

    @Override
    public <T> Supplier<T> register(Registry<? super T> registry, Identifier id, Supplier<T> obj) {
        T register = Registry.register(registry, id, obj.get());
        return () -> register;
    }

    @Override
    public ItemGroup itemGroup(Identifier id, Supplier<ItemStack> icon) {
        return FabricItemGroupBuilder.create(id).icon(icon).build();
    }
}
