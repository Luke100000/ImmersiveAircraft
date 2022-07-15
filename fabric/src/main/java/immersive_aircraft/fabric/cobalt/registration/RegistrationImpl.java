package immersive_aircraft.fabric.cobalt.registration;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import immersive_aircraft.cobalt.registration.Registration;
import immersive_aircraft.cobalt.registration.Registration.ProfessionFactory;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.mixin.object.builder.VillagerProfessionAccessor;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;

public class RegistrationImpl extends Registration.Impl {
    @Override
    public <T extends Entity> void registerEntityRenderer(EntityType<T> type, EntityRendererFactory<T> constructor) {
        EntityRendererRegistry.register(type, constructor);
    }

    @Override
    public <T> T registerEntityRenderer(Registry<? super T> registry, Identifier id, T obj) {
        return Registry.register(registry, id, obj);
    }

    @Override
    public Supplier<DefaultParticleType> simpleParticle() {
        return FabricParticleTypes::simple;
    }

    @Override
    public ItemGroup itemGroup(Identifier id, Supplier<ItemStack> icon) {
        return FabricItemGroupBuilder.create(id).icon(icon).build();
    }

    @Override
    public Function<Identifier, Activity> activity() {
        return null;
    }

    @Override
    public <T extends Sensor<?>> BiFunction<Identifier, Supplier<T>, SensorType<T>> sensor() {
        return null;
    }

    @Override
    public <U> BiFunction<Identifier, Optional<Codec<U>>, MemoryModuleType<U>> memoryModule() {
        return null;
    }

    @Override
    public <T extends LivingEntity> BiFunction<EntityType<T>, Supplier<Builder>, EntityType<T>> defaultEntityAttributes() {
        return (type, attributes) -> {
            //noinspection ConstantConditions
            FabricDefaultAttributeRegistry.register(type, attributes.get());
            return type;
        };
    }

    @Override
    public ProfessionFactory<VillagerProfession> profession() {
        return (id, poi, sound, items, sites) -> registerEntityRenderer(Registry.VILLAGER_PROFESSION, id, VillagerProfessionAccessor.create(id.toString().replace(':', '.'), poi, ImmutableSet.copyOf(items), ImmutableSet.copyOf(sites), sound));
    }
}
