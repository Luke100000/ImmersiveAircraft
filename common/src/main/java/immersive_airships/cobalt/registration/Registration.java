package immersive_airships.cobalt.registration;

import com.mojang.serialization.Codec;
import immersive_airships.entity.AirshipEntity;
import net.minecraft.block.Block;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class Registration {
    private static Impl INSTANCE;

    public static <T> T register(Registry<? super T> registry, Identifier id, T obj) {
        return INSTANCE.registerEntityRenderer(registry, id, obj);
    }

    public static <T extends AirshipEntity> void register(EntityType<?> type, EntityRendererFactory<T> constructor) {
        //noinspection unchecked
        INSTANCE.registerEntityRenderer((EntityType<T>) type, constructor);
    }

    public static class ObjectBuilders {
        public static class ItemGroups {
            public static ItemGroup create(Identifier id, Supplier<ItemStack> icon) {
                return INSTANCE.itemGroup(id, icon);
            }
        }

        public static class DefaultEntityAttributes {
            public static <T extends LivingEntity> EntityType<T> add(EntityType<T> type, Supplier<Builder> attributes) {
                return INSTANCE.<T>defaultEntityAttributes().apply(type, attributes);
            }
        }

        public static class Particles {
            public static DefaultParticleType simpleParticle() {
                return INSTANCE.simpleParticle().get();
            }
        }

        public static class Activities {
            public static Activity create(Identifier id) {
                return INSTANCE.activity().apply(id);
            }
        }

        public static class Sensors {
            public static <T extends Sensor<?>> SensorType<T> create(Identifier id, Supplier<T> factory) {
                return INSTANCE.<T>sensor().apply(id, factory);
            }
        }

        public static class Profession {
            public static ProfessionFactory<VillagerProfession> creator() {
                return INSTANCE.profession();
            }
        }
    }

    public static abstract class Impl {
        protected Impl() {
            INSTANCE = this;
        }

        public abstract <T extends Entity> void registerEntityRenderer(EntityType<T> type, EntityRendererFactory<T> constructor);

        public abstract <T> T registerEntityRenderer(Registry<? super T> registry, Identifier id, T obj);

        public abstract ItemGroup itemGroup(Identifier id, Supplier<ItemStack> icon);

        public abstract Supplier<DefaultParticleType> simpleParticle();

        public abstract Function<Identifier, Activity> activity();

        public abstract <T extends Sensor<?>> BiFunction<Identifier, Supplier<T>, SensorType<T>> sensor();

        public abstract <U> BiFunction<Identifier, Optional<Codec<U>>, MemoryModuleType<U>> memoryModule();

        public abstract <T extends LivingEntity> BiFunction<EntityType<T>, Supplier<Builder>, EntityType<T>> defaultEntityAttributes();

        public abstract ProfessionFactory<VillagerProfession> profession();
    }

    protected interface PoiFactory<T> {
        T apply(Identifier id, int ticketCount, int searchDistance, Block...blocks);
    }

    public interface ProfessionFactory<T> {
        T apply(Identifier id, PointOfInterestType workStation, @Nullable SoundEvent workSound,
                Iterable<Item> gatherableItems,
                Iterable<Block> secondaryJobSites);
    }
}
