package immersive_aircraft.entity;

import com.google.common.collect.Lists;
import com.mojang.math.Axis;
//import earth.terrarium.adastra.api.systems.GravityApi;
import immersive_aircraft.Main;
import immersive_aircraft.Sounds;
import immersive_aircraft.client.KeyBindings;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.config.Config;
import immersive_aircraft.data.VehicleDataLoader;
import immersive_aircraft.entity.misc.BoundingBoxDescriptor;
import immersive_aircraft.entity.misc.PositionDescriptor;
import immersive_aircraft.entity.misc.VehicleData;
import immersive_aircraft.network.c2s.CollisionMessage;
import immersive_aircraft.network.c2s.CommandMessage;
import immersive_aircraft.resources.bbmodel.AnimationVariableName;
import immersive_aircraft.resources.bbmodel.BBAnimationVariables;
import immersive_aircraft.util.InterpolatedFloat;
import net.minecraft.BlockUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract vehicle, which handles player input, collisions, passengers and
 * destruction
 */
public abstract class VehicleEntity extends Entity {
    public final ResourceLocation identifier;

    private static final EntityDataAccessor<Float> DATA_HEALTH = SynchedEntityData.defineId(VehicleEntity.class,
            EntityDataSerializers.FLOAT);

    protected static final EntityDataAccessor<Integer> DAMAGE_WOBBLE_TICKS = SynchedEntityData
            .defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DAMAGE_WOBBLE_SIDE = SynchedEntityData
            .defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> DAMAGE_WOBBLE_STRENGTH = SynchedEntityData
            .defineId(VehicleEntity.class, EntityDataSerializers.FLOAT);

    protected final boolean canExplodeOnCrash;

    protected static final EntityDataAccessor<Integer> BOOST = SynchedEntityData.defineId(VehicleEntity.class,
            EntityDataSerializers.INT);

    protected int lastTriedToExit;

    protected double x;
    protected double y;
    protected double z;

    protected float movementX;
    protected float movementY;
    protected float movementZ;

    public final InterpolatedFloat pressingInterpolatedX;
    public final InterpolatedFloat pressingInterpolatedY;
    public final InterpolatedFloat pressingInterpolatedZ;

    public float roll;
    public float prevRoll;

    public double lastX;
    public double lastY;
    public double lastZ;
    public double secondLastX;
    public double secondLastY;
    public double secondLastZ;

    public boolean adaptPlayerRotation = true;
    private int drowning;
    private InterpolationHandler interpolation;

    public float getRoll() {
        return roll;
    }

    public float getRoll(float tickDelta) {
        return Mth.lerp(tickDelta, prevRoll, getRoll());
    }

    @Override
    public void setXRot(float pitch) {
        float loops = (float) (Math.floor((pitch + 180f) / 360f) * 360f);
        pitch -= loops;
        xRotO -= loops;
        super.setXRot(pitch);
    }

    public void setZRot(float rot) {
        roll = rot;
    }

    public void boost() {
        boost(100);
    }

    public void boost(int ticks) {
        entityData.set(BOOST, ticks);
    }

    protected void applyBoost() {

    }

    public boolean canBoost() {
        return false;
    }

    public int getBoost() {
        return entityData.get(BOOST);
    }

    public VehicleData getVehicleData() {
        return VehicleDataLoader.get(identifier);
    }

    public int getPassengerSpace() {
        return getVehicleData().getPassengerPositions().size();
    }

    public VehicleEntity(EntityType<? extends VehicleEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world);

        this.canExplodeOnCrash = canExplodeOnCrash;
        blocksBuilding = true;

        pressingInterpolatedX = new InterpolatedFloat(getInputInterpolationSteps());
        pressingInterpolatedY = new InterpolatedFloat(getInputInterpolationSteps());
        pressingInterpolatedZ = new InterpolatedFloat(getInputInterpolationSteps());

        identifier = BuiltInRegistries.ENTITY_TYPE.getKey(getType());
    }

    @Override
    public float maxUpStep() {
        return 0.55f;
    }

    protected float getInputInterpolationSteps() {
        return 10;
    }

    @Override
    protected Entity.@NotNull MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DAMAGE_WOBBLE_TICKS, 0);
        entityData.define(DAMAGE_WOBBLE_SIDE, 1);
        entityData.define(DAMAGE_WOBBLE_STRENGTH, 0.0f);
        entityData.define(DATA_HEALTH, 1.0f);
        entityData.define(BOOST, 0);
    }

    @Override
    public boolean canCollideWith(@NotNull Entity other) {
        return canCollide(this, other);
    }

    public static boolean canCollide(Entity entity, Entity other) {
        return (other.canBeCollidedWith(entity) || other.isPushable()) && !entity.isPassengerOfSameVehicle(other);
    }

    @Override
    public boolean canBeCollidedWith(Entity other) {
        return true;
    }

    @Override
    public @NotNull Vec3 getRelativePortalPosition(Direction.@NotNull Axis portalAxis,
                                                   BlockUtil.@NotNull FoundRectangle portalRect) {
        return LivingEntity
                .resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(portalAxis, portalRect));
    }

    @Override
    public boolean skipAttackInteraction(@NotNull Entity attacker) {
        return hasPassenger(attacker) || super.skipAttackInteraction(attacker);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, @NotNull DamageSource source, float amount) {
        if (isInvulnerableToBase(source)) {
            return false;
        }

        if (isRemoved()) {
            return true;
        }

        // Creative player
        if (source.getEntity() instanceof Player player && player.getAbilities().instabuild) {
            dropInventory(serverLevel);
            discard();
            return true;
        }

        // Player on an empty vehicle is faster
        if (amount > 0 && source.getEntity() instanceof Player && getPassengers().isEmpty() && source.isDirect()) {
            amount = Math.max(5.0f, amount);
        }

        setDamageWobbleSide(-getDamageWobbleSide());
        setDamageWobbleTicks(10);

        // todo different per vehicle
        setDamageWobbleStrength((float) (getDamageWobbleStrength()
                + Math.sqrt(amount) * 5.0f / (1.0f + getDamageWobbleStrength() * 0.05f)));

        gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());

        boolean force = !(source.getDirectEntity() instanceof Player);

        applyDamage(serverLevel, amount / getDurability() / Config.getInstance().damagePerHealthPoint, force);

        return true;
    }

    private void applyDamage(ServerLevel level, float amount, boolean force) {
        if (isRemoved()) {
            return;
        }

        float health = getHealth() - amount;
        if (health <= 0) {
            setHealth(0);
            // Saving cords for explode (if enabled)
            double x = getX();
            double y = getY();
            double z = getZ();

            discard();

            // Explode if destroyed by force
            if (force && canExplodeOnCrash && Config.getInstance().enableCrashExplosion) {
                level.explode(this, x, y, z,
                        Config.getInstance().crashExplosionRadius,
                        Config.getInstance().enableCrashFire,
                        Config.getInstance().enableCrashBlockDestruction ? Level.ExplosionInteraction.MOB
                                : Level.ExplosionInteraction.NONE);
            }

            // Drop stuff if enabled
            if (level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)
                    && Config.getInstance().enableDropsForNonPlayer) {
                dropInventory(level);
                drop(level);
            }
        } else {
            setHealth(health);
        }
    }

    private void repair(float amount) {
        float health = Math.min(1.0f, getHealth() + amount);
        setHealth(health);
    }

    public float getDurability() {
        return 1.0f;
    }

    protected void drop(ServerLevel level) {
        if (Config.getInstance().dropAircraft) {
            ItemStack stack = new ItemStack(asItem());
            addItemTag(stack);
            spawnAtLocation(level, stack);
        }
    }

    protected void dropInventory(ServerLevel serverLevel) {
        // nothing
    }

    @Override
    public void onAboveBubbleColumn(boolean drag, BlockPos pos) {
        level().addParticle(ParticleTypes.SPLASH, getX() + (double) random.nextFloat(), getY() + 0.7,
                getZ() + (double) random.nextFloat(), 0.0, 0.0, 0.0);
        if (random.nextInt(20) == 0) {
            level().playLocalSound(getX(), getY(), getZ(), getSwimSplashSound(), getSoundSource(), 1.0f,
                    0.8f + 0.4f * random.nextFloat(), false);
        }
        gameEvent(GameEvent.SPLASH, getControllingPassenger());
    }

    public Item asItem() {
        return Items.STICK;
    }

    @Override
    public void animateHurt(float yaw) {
        setDamageWobbleSide(-getDamageWobbleSide());
        setDamageWobbleTicks(10);
        setDamageWobbleStrength(getDamageWobbleStrength() * 11.0f);
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public InterpolationHandler getInterpolation() {
        if (this.interpolation == null) {
            this.interpolation = new InterpolationHandler(this, 10);
        }
        return this.interpolation;
    }

    private static float getMovementMultiplier(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0f;
        }
        return positive ? 1.0f : -1.0f;
    }

    protected boolean useAirplaneControls() {
        return false;
    }

    @Override
    public void tick() {
        if (tickCount % 10 == 0) {
            secondLastX = lastX;
            secondLastY = lastY;
            secondLastZ = lastZ;

            lastX = getX();
            lastY = getY();
            lastZ = getZ();

            if (secondLastX == 0 && secondLastY == 0 && secondLastZ == 0) {
                secondLastX = lastX;
                secondLastY = lastY;
                secondLastZ = lastZ;
            }
        }

        // pilot
        if (!getPassengers().isEmpty()) {
            tickPilot();
        }

        // wobble
        if (getDamageWobbleTicks() > 0) {
            setDamageWobbleTicks(getDamageWobbleTicks() - 1);
        }
        if (getDamageWobbleStrength() > 0.0f) {
            setDamageWobbleStrength(getDamageWobbleStrength() - 1.0f);
        }

        super.tick();

        // interpolate
        handleClientSync();

        int boost = getBoost();
        if (boost > 0) {
            entityData.set(BOOST, boost - 1);
        }

        // if it's the right side, update the velocity
        if (isLocalClientAuthoritative()) {
            updateVelocity();

            // boost
            if (boost > 0) {
                applyBoost();
            }

            updateController();

            move(MoverType.SELF, getDeltaMovement());
        }

        applyEffectsFromBlocks();

        // auto enter
        List<Entity> list = level().getEntities(this, getBoundingBox().inflate(0.2f, -0.01f, 0.2f),
                EntitySelector.pushableBy(this));
        if (!list.isEmpty()) {
            boolean bl = (level() instanceof ServerLevel) && !(getControllingPassenger() instanceof Player);
            for (Entity entity : list) {
                if (entity.hasPassenger(this))
                    continue;
                if (bl && getPassengers().size() < (getPassengerSpace() - 1) && !entity.isPassenger()
                        && entity.getBbWidth() < getBbWidth() && entity instanceof LivingEntity
                        && !(entity instanceof WaterAnimal) && !(entity instanceof Player)) {
                    entity.startRiding(this);
                }
            }
        }

        // interpolate keys for visual feedback
        if (isLocalClientAuthoritative()) {
            pressingInterpolatedX.update(movementX);
            pressingInterpolatedY.update(movementY);
            pressingInterpolatedZ.update(movementZ);
        }

        tickDamageParticles();

        // Automatic regeneration if requested
        if (level() instanceof ServerLevel serverLevel) {
            int t = Config.getInstance().regenerateHealthEveryNTicks;
            if (t > 0 && serverLevel.getGameTime() % t == 0) {
                repair(0.05f / getDurability());
            }
        }
    }

    private void tickDamageParticles() {
        if (level() instanceof ClientLevel clientLevel && random.nextFloat() > getHealth()) {
            // Damage particles
            List<AABB> shapes = getShapes();
            AABB shape = shapes.get(random.nextInt(shapes.size()));
            Vec3 center = shape.getCenter();
            double x = center.x + shape.getXsize() * (random.nextDouble() - 0.5) * 1.5;
            double y = center.y + shape.getYsize() * (random.nextDouble() - 0.5) * 1.5;
            double z = center.z + shape.getZsize() * (random.nextDouble() - 0.5) * 1.5;

            Vec3 speed = getSpeedVector();
            clientLevel.addParticle(ParticleTypes.SMOKE, x, y, z, speed.x, speed.y, speed.z);
            if (getHealth() < 0.5) {
                clientLevel.addParticle(ParticleTypes.SMALL_FLAME, x, y, z, speed.x, speed.y, speed.z);
            }
        }

        // Drowning particles
        if (isUnderWater() && drowning < 200) {
            drowning++;

            for (AABB shape : getShapes()) {
                Vec3 center = shape.getCenter();
                double x = center.x + shape.getXsize() * (random.nextDouble() - 0.5) * 1.5;
                double y = center.y + shape.getYsize() * (random.nextDouble() - 0.5) * 1.5;
                double z = center.z + shape.getZsize() * (random.nextDouble() - 0.5) * 1.5;
                this.level().addParticle(ParticleTypes.BUBBLE, x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }

    private void tickPilot() {
        for (Entity entity : getPassengers()) {
            if (entity instanceof Player player && player.isLocalPlayer()) {
                if (KeyBindings.down.isDown() && onGround() && getDeltaMovement().length() < 0.01) {
                    player.displayClientMessage(
                            Component.translatable("mount.onboard", KeyBindings.dismount.getTranslatedKeyMessage()),
                            true);
                }

                if (Main.debouncingGetter.is(Main.Key.DISMOUNT)) {
                    if (onGround() || tickCount - lastTriedToExit < 20) {
                        NetworkHandler
                                .sendToServer(new CommandMessage(CommandMessage.Key.DISMOUNT, getDeltaMovement()));
                        player.setJumping(false);
                    } else {
                        lastTriedToExit = tickCount;
                        player.displayClientMessage(Component.translatable("immersive_aircraft.tried_dismount"), true);
                    }
                }

                if (Main.debouncingGetter.is(Main.Key.BOOST) && canBoost()) {
                    NetworkHandler.sendToServer(new CommandMessage(CommandMessage.Key.BOOST, getDeltaMovement()));
                    Vec3 p = position();
                    level().playLocalSound(p.x(), p.y(), p.z(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL,
                            1.0f, 1.0f, true);
                }
            }
        }

        // controls
        Entity pilot = getPassengers().getFirst();
        if (pilot instanceof Player player && player.isLocalPlayer()) {
            setInputs(getMovementMultiplier(
                    KeyBindings.left.isDown(),
                    KeyBindings.right.isDown()),
                    getMovementMultiplier(
                            KeyBindings.up.isDown(),
                            KeyBindings.down.isDown()),
                    getMovementMultiplier(
                            useAirplaneControls() ? KeyBindings.push.isDown() : KeyBindings.forward.isDown(),
                            useAirplaneControls() ? KeyBindings.pull.isDown() : KeyBindings.backward.isDown()));
        } else {
            setInputs(0, 0, 0);
        }
    }

    private void handleClientSync() {
        if (isLocalClientAuthoritative()) {
            getInterpolation().cancel();
            syncPacketPositionCodec(getX(), getY(), getZ());
        }
        getInterpolation().interpolate();
    }

    protected abstract void updateVelocity();

    @Override
    protected double getDefaultGravity() {
        return 0.04f; // * (CompatUtil.isModLoaded("ad_astra") ? GravityApi.API.getGravity(level(),
                      // BlockPos.containing(getEyePosition())) : 1);
    }

    protected abstract void updateController();

    @Override
    public void positionRider(@NotNull Entity passenger, @NotNull MoveFunction positionUpdater) {
        if (!hasPassenger(passenger)) {
            return;
        }

        Matrix4f transform = getVehicleTransform();

        int size = getPassengers().size() - 1;
        List<List<PositionDescriptor>> positions = getVehicleData().getPassengerPositions();
        if (size < positions.size()) {
            int i = getPassengers().indexOf(passenger);
            if (i >= 0 && i < positions.get(size).size()) {
                PositionDescriptor positionDescriptor = positions.get(size).get(i);

                float x = positionDescriptor.x();
                float y = positionDescriptor.y();
                float z = positionDescriptor.z();

                // Passenger offset
                Vec3 attachmentPoint = passenger.getVehicleAttachmentPoint(this);
                x -= (float) attachmentPoint.x;
                y -= (float) attachmentPoint.y;
                z -= (float) attachmentPoint.z;

                Vector4f worldPosition = transformPosition(transform, x, y, z);

                passenger.setPos(worldPosition.x, worldPosition.y, worldPosition.z);

                if (adaptPlayerRotation) {
                    passenger.setYRot(passenger.getYRot() + (getYRot() - yRotO));
                    passenger.setYHeadRot(passenger.getYHeadRot() + (getYRot() - yRotO));
                }

                positionUpdater.accept(passenger, worldPosition.x, worldPosition.y, worldPosition.z);

                copyEntityData(passenger);
                if (passenger instanceof Animal animal && size > 1) {
                    int angle = passenger.getId() % 2 == 0 ? 90 : 270;
                    passenger.setYBodyRot(animal.yBodyRot + (float) angle);
                    passenger.setYHeadRot(passenger.getYHeadRot() + (float) angle);
                }
            }
        }
    }

    protected Vec3 getDismountOffset(double vehicleWidth, double passengerWidth) {
        double offset = (vehicleWidth + passengerWidth + (double) 1.0E-5f) / 2.0;
        float yaw = getYRot() + getDismountRotation();
        float x = -Mth.sin(yaw * ((float) Math.PI / 180));
        float z = Mth.cos(yaw * ((float) Math.PI / 180));
        float n = Math.max(Math.abs(x), Math.abs(z));
        return new Vec3((double) x * offset / (double) n, 0.0, (double) z * offset / (double) n);
    }

    protected float getDismountRotation() {
        return 90.0f;
    }

    @Override
    public @NotNull Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 vec3d = getDismountOffset(getBbWidth() * Mth.SQRT_OF_TWO, passenger.getBbWidth() * Mth.SQRT_OF_TWO);
        double ox = getX() + vec3d.x;
        double oz = getZ() + vec3d.z;
        BlockPos exitPos = new BlockPos((int) ox, (int) getY(), (int) oz);
        BlockPos floorPos = exitPos.below();
        if (!level().isWaterAt(floorPos)) {
            ArrayList<Vec3> list = Lists.newArrayList();
            double exitHeight = level().getBlockFloorHeight(exitPos);
            if (DismountHelper.isBlockFloorValid(exitHeight)) {
                list.add(new Vec3(ox, (double) exitPos.getY() + exitHeight, oz));
            }
            double floorHeight = level().getBlockFloorHeight(floorPos);
            if (DismountHelper.isBlockFloorValid(floorHeight)) {
                list.add(new Vec3(ox, (double) floorPos.getY() + floorHeight, oz));
            }
            for (Pose entityPose : passenger.getDismountPoses()) {
                for (Vec3 vec3d2 : list) {
                    if (!DismountHelper.canDismountTo(level(), vec3d2, passenger, entityPose))
                        continue;
                    passenger.setPose(entityPose);
                    return vec3d2;
                }
            }
        }

        return super.getDismountLocationForPassenger(passenger);
    }

    public void copyEntityData(Entity entity) {
        entity.setYBodyRot(getYRot());
        float f = Mth.wrapDegrees(entity.getYRot() - getYRot());
        float g = Mth.clamp(f, -105.0f, 105.0f);
        entity.yRotO += g - f;
        entity.setYRot(entity.getYRot() + g - f);
        entity.setYHeadRot(entity.getYRot());
    }

    @Override
    public void onPassengerTurned(@NotNull Entity passenger) {
        copyEntityData(passenger);
    }

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput output) {
        output.putFloat("VehicleHealth", getHealth());
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {
        setHealth(input.getFloatOr("VehicleHealth", getHealth()));
    }

    public void addItemTag(ItemStack stack) {
        // Store plane's name
        if (hasCustomName()) {
            stack.set(DataComponents.CUSTOM_NAME, getCustomName());
        }
    }

    public void readItemTag(ItemStack stack) {
        // Read plane's name
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            setCustomName(stack.get(DataComponents.CUSTOM_NAME));
        }
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (getHealth() < 1.0f && (player.isShiftKeyDown() || !Config.getInstance().requireShiftForRepair)
                && !hasPassenger(player)) {
            if (level() instanceof ServerLevel serverLevel) {
                player.causeFoodExhaustion(Config.getInstance().repairExhaustion);
                repair(Config.getInstance().repairSpeed);

                // Repair message
                MutableComponent component = Component.translatable("immersive_aircraft.repair",
                        (int) (getHealth() * 100.0f));
                if (getHealth() < 0.33) {
                    component.withStyle(ChatFormatting.RED);
                } else if (getHealth() < 0.66) {
                    component.withStyle(ChatFormatting.GOLD);
                } else {
                    component.withStyle(ChatFormatting.GREEN);
                }
                player.displayClientMessage(component, true);

                serverLevel.playSound(null, getX(), getY(), getZ(), Sounds.REPAIR.get(), SoundSource.NEUTRAL, 1.0f,
                        0.7f + random.nextFloat() * 0.2f);
            } else {
                // Repair particles
                for (AABB shape : getAdditionalShapes()) {
                    for (int i = 0; i < 5; i++) {
                        Vec3 center = shape.getCenter();
                        double x = center.x + shape.getXsize() * (random.nextDouble() - 0.5) * 1.5;
                        double y = center.y + shape.getYsize() * (random.nextDouble() - 0.5) * 1.5;
                        double z = center.z + shape.getZsize() * (random.nextDouble() - 0.5) * 1.5;
                        level().addParticle(ParticleTypes.COMPOSTER, x, y, z, 0, random.nextDouble(), 0);
                    }
                }
            }

            return InteractionResult.CONSUME;
        }
        if (!isValidDimension()) {
            player.displayClientMessage(Component.translatable("immersive_aircraft.invalid_dimension"), true);
            return InteractionResult.FAIL;
        }
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        if (level() instanceof ServerLevel) {
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        if (hasPassenger(player)) {
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void move(@NotNull MoverType movementType, @NotNull Vec3 movement) {
        Vec3 prediction = position().add(movement);
        super.move(movementType, movement);

        // Collision damage
        if ((verticalCollision || horizontalCollision) && level().isClientSide()
                && Config.getInstance().collisionDamage) {
            double maxPossibleError = movement.length();
            double error = prediction.distanceTo(position());
            if (error <= maxPossibleError) {
                float collision = (float) (error - (verticalCollision ? Math.abs(getGravity()) : 0.0)) - 0.05f;
                if (collision > 0) {
                    float repeat = 1.0f - (getDamageWobbleTicks() + 1) / 10.0f;
                    if (repeat > 0.0001f) {
                        float damage = collision * Config.getInstance().collisionDamageMultiplier * repeat * repeat;
                        NetworkHandler.sendToServer(new CollisionMessage(damage));
                    }
                }
            }
        }
    }

    @Override
    protected void checkFallDamage(double heightDifference, boolean onGround, @NotNull BlockState landedState,
            @NotNull BlockPos landedPosition) {

    }

    public void setDamageWobbleStrength(float wobbleStrength) {
        entityData.set(DAMAGE_WOBBLE_STRENGTH, wobbleStrength);
    }

    public float getDamageWobbleStrength() {
        return entityData.get(DAMAGE_WOBBLE_STRENGTH);
    }

    public void setDamageWobbleTicks(int wobbleTicks) {
        entityData.set(DAMAGE_WOBBLE_TICKS, wobbleTicks);
    }

    public int getDamageWobbleTicks() {
        return entityData.get(DAMAGE_WOBBLE_TICKS);
    }

    public void setDamageWobbleSide(int side) {
        entityData.set(DAMAGE_WOBBLE_SIDE, side);
    }

    public int getDamageWobbleSide() {
        return entityData.get(DAMAGE_WOBBLE_SIDE);
    }

    public float getHealth() {
        return entityData.get(DATA_HEALTH);
    }

    public void setHealth(float damage) {
        entityData.set(DATA_HEALTH, damage);
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity passenger) {
        return getPassengers().size() < getPassengerSpace() && !isEyeInFluid(FluidTags.WATER);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        if (getFirstPassenger() instanceof LivingEntity le) {
            return le;
        } else {
            return super.getControllingPassenger();
        }
    }

    @Nullable
    public Entity getGunner(int offset) {
        List<Entity> passengers = getPassengers();
        return passengers.isEmpty() ? null : passengers.get(Math.max(0, passengers.size() - 1 - offset));
    }

    public void setInputs(float x, float y, float z) {
        this.movementX = x;
        this.movementY = y;
        this.movementZ = z;
    }

    public boolean canTurnOnEngine(Entity pilot) {
        return pilot instanceof Player;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(asItem());
    }

    public boolean isWithinParticleRange() {
        return Main.cameraGetter.getPosition().distanceToSqr(position()) < 1024;
    }

    protected Vector4f transformPosition(Matrix4f transform, float x, float y, float z) {
        return transform.transform(new Vector4f(x, y, z, 1));
    }

    protected Vector3f transformVector(float x, float y, float z) {
        return transformVector(getVehicleNormalTransform(), x, y, z);
    }

    protected Vector3f transformVectorQuantized(float x, float y, float z) {
        return transformVector(getVehicleNormalTransformQuantized(), x, y, z);
    }

    protected Vector3f transformVector(Matrix3f transform, float x, float y, float z) {
        return transform.transform(new Vector3f(x, y, z));
    }

    public Matrix4f getVehicleTransform() {
        Matrix4f transform = new Matrix4f();
        transform.translate((float) getX(), (float) getY(), (float) getZ());
        transform.rotate(Axis.YP.rotationDegrees(-getYRot()));
        transform.rotate(Axis.XP.rotationDegrees(getXRot()));
        transform.rotate(Axis.ZP.rotationDegrees(getRoll()));
        return transform;
    }

    private float quantize(float value) {
        int floor = Mth.floor(value * 256.0f / 360.0f);
        return (floor * 360) / 256.0f;
    }

    public Matrix3f getVehicleNormalTransformQuantized() {
        Matrix3f transform = new Matrix3f();
        transform.rotate(Axis.YP.rotationDegrees(-quantize(getYRot())));
        transform.rotate(Axis.XP.rotationDegrees(quantize(getXRot())));
        transform.rotate(Axis.ZP.rotationDegrees(quantize(getRoll())));
        return transform;
    }

    public Matrix3f getVehicleNormalTransform() {
        Matrix3f transform = new Matrix3f();
        transform.rotate(Axis.YP.rotationDegrees(-getYRot()));
        transform.rotate(Axis.XP.rotationDegrees(getXRot()));
        transform.rotate(Axis.ZP.rotationDegrees(getRoll()));
        return transform;
    }

    public Vector3f getForwardDirection() {
        return transformVector(0.0f, 0.0f, 1.0f);
    }

    public Vector3f getRightDirection() {
        Vector3f f = transformVector(1.0f, 0.0f, 0.0f);
        return new Vector3f(f.x(), f.y(), f.z());
    }

    public Vector3f getTopDirection() {
        return transformVector(0.0f, 1.0f, 0.0f);
    }

    // This field is used in some addons!
    @SuppressWarnings("unused")
    protected static final Vector4f ZERO_VEC4 = new Vector4f();

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d = Config.getInstance().renderDistance * getViewScale();
        return distance < d * d;
    }

    public void chill() {

    }

    public Vec3 toVec3d(Vector3f v) {
        return new Vec3(v.x, v.y, v.z);
    }

    public boolean isValidDimension() {
        return Config.getInstance().validDimensions.getOrDefault(this.level().dimension().location().toString(), true);
    }

    protected AABB getOffsetBoundingBox(BoundingBoxDescriptor descriptor) {
        Vector3f center = transformVectorQuantized(descriptor.x(), descriptor.y(), descriptor.z());
        return new AABB(
                center.x() - descriptor.width() / 2.0 + getX(),
                center.y() - descriptor.height() / 2.0 + getY(),
                center.z() - descriptor.width() / 2.0 + getZ(),
                center.x() + descriptor.width() / 2.0 + getX(),
                center.y() + descriptor.height() / 2.0 + getY(),
                center.z() + descriptor.width() / 2.0 + getZ());
    }

    public List<AABB> getAdditionalShapes() {
        return getVehicleData().getBoundingBoxes().stream().map(this::getOffsetBoundingBox).toList();
    }

    public List<AABB> getShapes() {
        List<AABB> shapes = new ArrayList<>(getAdditionalShapes());
        shapes.add(getBoundingBox());
        return shapes;
    }

    public Vec3 getSpeedVector() {
        return new Vec3((lastX - secondLastX) / 10.0f, (lastY - secondLastY) / 10.0f, (lastZ - secondLastZ) / 10.0f);
    }

    public boolean isPilotCreative() {
        return getControllingPassenger() instanceof Player player && player.isCreative();
    }

    public double getZoom() {
        return 0.0;
    }

    public AABB getBoundingBoxForCulling() {
        AABB box = super.getBoundingBox();
        for (AABB additionalShape : getAdditionalShapes()) {
            box = box.minmax(additionalShape);
        }
        return box;
    }


    public void setAnimationVariables(BBAnimationVariables animationVariables, float tickDelta) {
        animationVariables.set(AnimationVariableName.PRESSING_INTERPOLATED_X, pressingInterpolatedX.getSmooth(tickDelta));
        animationVariables.set(AnimationVariableName.PRESSING_INTERPOLATED_Y, pressingInterpolatedY.getSmooth(tickDelta));
        animationVariables.set(AnimationVariableName.PRESSING_INTERPOLATED_Z, pressingInterpolatedZ.getSmooth(tickDelta));

        Vec3 speed = getSpeedVector();
        animationVariables.set(AnimationVariableName.VELOCITY_X, (float) speed.x);
        animationVariables.set(AnimationVariableName.VELOCITY_Y, (float) speed.y);
        animationVariables.set(AnimationVariableName.VELOCITY_Z, (float) speed.z);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return super.getDisplayName();
    }
}