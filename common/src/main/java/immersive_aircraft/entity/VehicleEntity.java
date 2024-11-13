package immersive_aircraft.entity;

import com.google.common.collect.Lists;
import com.mojang.math.Axis;
import earth.terrarium.adastra.api.systems.GravityApi;
import immersive_aircraft.CompatUtil;
import immersive_aircraft.Main;
import immersive_aircraft.Sounds;
import immersive_aircraft.client.KeyBindings;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.config.Config;
import immersive_aircraft.data.VehicleDataLoader;
import immersive_aircraft.entity.misc.BoundingBoxDescriptor;
import immersive_aircraft.entity.misc.PositionDescriptor;
import immersive_aircraft.network.c2s.CollisionMessage;
import immersive_aircraft.network.c2s.CommandMessage;
import immersive_aircraft.resources.bbmodel.BBAnimationVariables;
import immersive_aircraft.util.InterpolatedFloat;
import net.minecraft.BlockUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
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
 * Abstract vehicle, which handles player input, collisions, passengers and destruction
 */
public abstract class VehicleEntity extends Entity {
    public final ResourceLocation identifier;

    private static final EntityDataAccessor<Float> DATA_HEALTH = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.FLOAT);

    protected static final EntityDataAccessor<Integer> DAMAGE_WOBBLE_TICKS = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DAMAGE_WOBBLE_SIDE = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> DAMAGE_WOBBLE_STRENGTH = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.FLOAT);

    protected final boolean canExplodeOnCrash;

    protected static final EntityDataAccessor<Integer> BOOST = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);

    protected int interpolationSteps;
    protected int lastTriedToExit;

    protected double x;
    protected double y;
    protected double z;

    protected double serverYRot;
    protected double serverXRot;

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

    public List<List<PositionDescriptor>> getPassengerPositions() {
        return VehicleDataLoader.get(identifier).getPassengerPositions();
    }

    public int getPassengerSpace() {
        return getPassengerPositions().size();
    }

    public VehicleEntity(EntityType<? extends VehicleEntity> entityType, Level world, boolean canExplodeOnCrash) {
        super(entityType, world);

        this.canExplodeOnCrash = canExplodeOnCrash;
        blocksBuilding = true;
        setMaxUpStep(0.55f);

        pressingInterpolatedX = new InterpolatedFloat(getInputInterpolationSteps());
        pressingInterpolatedY = new InterpolatedFloat(getInputInterpolationSteps());
        pressingInterpolatedZ = new InterpolatedFloat(getInputInterpolationSteps());

        identifier = BuiltInRegistries.ENTITY_TYPE.getKey(getType());
    }

    public void fromItemStack(ItemStack stack) {
        if (stack.hasTag()) {
            assert stack.getTag() != null;
            readItemTag(stack.getTag());
        }
    }

    protected float getInputInterpolationSteps() {
        return 10;
    }

    @Override
    protected float getEyeHeight(@NotNull Pose pose, EntityDimensions dimensions) {
        return dimensions.height;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData() {
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
        return (other.canBeCollidedWith() || other.isPushable()) && !entity.isPassengerOfSameVehicle(other);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected Vec3 getRelativePortalPosition(Direction.@NotNull Axis portalAxis, BlockUtil.@NotNull FoundRectangle portalRect) {
        return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(portalAxis, portalRect));
    }

    @Override
    public boolean skipAttackInteraction(@NotNull Entity attacker) {
        return hasPassenger(attacker) || super.skipAttackInteraction(attacker);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }

        if (level().isClientSide || isRemoved()) {
            return true;
        }

        // Creative player
        if (source.getEntity() instanceof Player player && player.getAbilities().instabuild) {
            dropInventory();
            discard();
            return true;
        }

        // Player on an empty vehicle is faster
        if (amount > 0 && source.getEntity() instanceof Player && getPassengers().isEmpty() && !source.isIndirect()) {
            amount = Math.max(5.0f, amount);
        }

        setDamageWobbleSide(-getDamageWobbleSide());
        setDamageWobbleTicks(10);

        // todo different per vehicle
        setDamageWobbleStrength((float) (getDamageWobbleStrength() + Math.sqrt(amount) * 5.0f / (1.0f + getDamageWobbleStrength() * 0.05f)));

        gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());

        boolean force = !(source.getDirectEntity() instanceof Player);

        applyDamage(amount / getDurability() / Config.getInstance().damagePerHealthPoint, force);

        return true;
    }

    private void applyDamage(float amount, boolean force) {
        if (isRemoved()) {
            return;
        }

        float health = getHealth() - amount;
        if (health <= 0) {
            setHealth(0);

            // Explode if destroyed by force
            if (force && canExplodeOnCrash && Config.getInstance().enableCrashExplosion) {
                level().explode(this, getX(), getY(), getZ(),
                        Config.getInstance().crashExplosionRadius,
                        Config.getInstance().enableCrashFire,
                        Config.getInstance().enableCrashBlockDestruction ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
            }

            // Drop stuff if enabled
            if (level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS) && Config.getInstance().enableDropsForNonPlayer) {
                dropInventory();
                drop();
            }

            discard();
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

    protected void drop() {
        ItemStack stack = new ItemStack(asItem());
        CompoundTag tag = stack.getOrCreateTag();
        addItemTag(tag);
        spawnAtLocation(stack);
    }

    protected void dropInventory() {
        // nothing
    }

    @Override
    public void onAboveBubbleCol(boolean drag) {
        level().addParticle(ParticleTypes.SPLASH, getX() + (double) random.nextFloat(), getY() + 0.7, getZ() + (double) random.nextFloat(), 0.0, 0.0, 0.0);
        if (random.nextInt(20) == 0) {
            level().playLocalSound(getX(), getY(), getZ(), getSwimSplashSound(), getSoundSource(), 1.0f, 0.8f + 0.4f * random.nextFloat(), false);
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
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
        this.x = x;
        this.y = y;
        this.z = z;
        serverYRot = yaw;
        serverXRot = pitch;
        this.interpolationSteps = 10;
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
        if (isControlledByLocalInstance()) {
            updateVelocity();

            // boost
            if (boost > 0) {
                applyBoost();
            }

            updateController();

            move(MoverType.SELF, getDeltaMovement());
        }

        checkInsideBlocks();

        // auto enter
        List<Entity> list = level().getEntities(this, getBoundingBox().inflate(0.2f, -0.01f, 0.2f), EntitySelector.pushableBy(this));
        if (!list.isEmpty()) {
            boolean bl = !level().isClientSide && !(getControllingPassenger() instanceof Player);
            for (Entity entity : list) {
                if (entity.hasPassenger(this)) continue;
                if (bl && getPassengers().size() < (getPassengerSpace() - 1) && !entity.isPassenger() && entity.getBbWidth() < getBbWidth() && entity instanceof LivingEntity && !(entity instanceof WaterAnimal) && !(entity instanceof Player)) {
                    entity.startRiding(this);
                }
            }
        }

        // interpolate keys for visual feedback
        if (isControlledByLocalInstance()) {
            pressingInterpolatedX.update(movementX);
            pressingInterpolatedY.update(movementY);
            pressingInterpolatedZ.update(movementZ);
        }

        tickDamageParticles();

        // Automatic regeneration if requested
        if (!level().isClientSide) {
            int t = Config.getInstance().regenerateHealthEveryNTicks;
            if (t > 0 && level().getGameTime() % t == 0) {
                repair(0.05f / getDurability());
            }
        }
    }

    private void tickDamageParticles() {
        if (level().isClientSide && random.nextFloat() > getHealth()) {
            // Damage particles
            List<AABB> shapes = getShapes();
            AABB shape = shapes.get(random.nextInt(shapes.size()));
            Vec3 center = shape.getCenter();
            double x = center.x + shape.getXsize() * (random.nextDouble() - 0.5) * 1.5;
            double y = center.y + shape.getYsize() * (random.nextDouble() - 0.5) * 1.5;
            double z = center.z + shape.getZsize() * (random.nextDouble() - 0.5) * 1.5;

            Vec3 speed = getSpeedVector();
            level().addParticle(ParticleTypes.SMOKE, x, y, z, speed.x, speed.y, speed.z);
            if (getHealth() < 0.5) {
                level().addParticle(ParticleTypes.SMALL_FLAME, x, y, z, speed.x, speed.y, speed.z);
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
                    player.displayClientMessage(Component.translatable("mount.onboard", KeyBindings.dismount.getTranslatedKeyMessage()), true);
                }

                if (KeyBindings.dismount.consumeClick()) {
                    if (onGround() || tickCount - lastTriedToExit < 20) {
                        NetworkHandler.sendToServer(new CommandMessage(CommandMessage.Key.DISMOUNT, getDeltaMovement()));
                        player.setJumping(false);
                    } else {
                        lastTriedToExit = tickCount;
                        player.displayClientMessage(Component.translatable("immersive_aircraft.tried_dismount"), true);
                    }
                }

                if (KeyBindings.boost.consumeClick() && canBoost()) {
                    NetworkHandler.sendToServer(new CommandMessage(CommandMessage.Key.BOOST, getDeltaMovement()));
                    Vec3 p = position();
                    level().playLocalSound(p.x(), p.y(), p.z(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 1.0f, 1.0f, true);
                }
            }
        }

        //controls
        Entity pilot = getPassengers().get(0);
        if (pilot instanceof Player player && player.isLocalPlayer()) {
            setInputs(getMovementMultiplier(
                            KeyBindings.left.isDown(),
                            KeyBindings.right.isDown()
                    ), getMovementMultiplier(
                            KeyBindings.up.isDown(),
                            KeyBindings.down.isDown()
                    ),
                    getMovementMultiplier(
                            useAirplaneControls() ? KeyBindings.push.isDown() : KeyBindings.forward.isDown(),
                            useAirplaneControls() ? KeyBindings.pull.isDown() : KeyBindings.backward.isDown()
                    )
            );
        } else {
            setInputs(0, 0, 0);
        }
    }

    private void handleClientSync() {
        if (isControlledByLocalInstance()) {
            interpolationSteps = 0;
            syncPacketPositionCodec(getX(), getY(), getZ());
        }
        if (interpolationSteps <= 0) {
            return;
        }
        double interpolatedX = getX() + (x - getX()) / (double) interpolationSteps;
        double interpolatedY = getY() + (y - getY()) / (double) interpolationSteps;
        double interpolatedZ = getZ() + (z - getZ()) / (double) interpolationSteps;
        double interpolatedYaw = Mth.wrapDegrees(serverYRot - (double) getYRot());
        setYRot(getYRot() + (float) interpolatedYaw / (float) interpolationSteps);
        setXRot(getXRot() + (float) (serverXRot - (double) getXRot()) / (float) interpolationSteps);

        setPos(interpolatedX, interpolatedY, interpolatedZ);
        setRot(getYRot(), getXRot());

        --interpolationSteps;
    }

    protected abstract void updateVelocity();

    protected float getGravity() {
        return -0.04f * (CompatUtil.isModLoaded("ad_astra") ? GravityApi.API.getGravity(level(), BlockPos.containing(getEyePosition())) : 1);
    }

    protected abstract void updateController();

    @Override
    public void positionRider(@NotNull Entity passenger, @NotNull MoveFunction positionUpdater) {
        if (!hasPassenger(passenger)) {
            return;
        }

        Matrix4f transform = getVehicleTransform();

        int size = getPassengers().size() - 1;
        List<List<PositionDescriptor>> positions = getPassengerPositions();
        if (size < positions.size()) {
            int i = getPassengers().indexOf(passenger);
            if (i >= 0 && i < positions.get(size).size()) {
                PositionDescriptor positionDescriptor = positions.get(size).get(i);

                float x = positionDescriptor.x();
                float y = positionDescriptor.y();
                float z = positionDescriptor.z();

                //animals are thicc
                if (passenger instanceof Animal) {
                    z += 0.2f;
                }

                y += (float) passenger.getMyRidingOffset();

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

    private Vec3 getDismountOffset(double vehicleWidth, double passengerWidth) {
        double offset = (vehicleWidth + passengerWidth + (double) 1.0E-5f) / 2.0;
        float yaw = getYRot() + 90.0f;
        float x = -Mth.sin(yaw * ((float) Math.PI / 180));
        float z = Mth.cos(yaw * ((float) Math.PI / 180));
        float n = Math.max(Math.abs(x), Math.abs(z));
        return new Vec3((double) x * offset / (double) n, 0.0, (double) z * offset / (double) n);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
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
                    if (!DismountHelper.canDismountTo(level(), vec3d2, passenger, entityPose)) continue;
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
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putFloat("VehicleHealth", getHealth());
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        if (tag.contains("VehicleHealth")) {
            setHealth(tag.getFloat("VehicleHealth"));
        }
    }

    protected void addItemTag(@NotNull CompoundTag tag) {
        // Store plane's name
        CompoundTag displayTag = new CompoundTag();
        tag.put("display", displayTag);
        if (hasCustomName()) {
            displayTag.putString("Name", Component.Serializer.toJson(getCustomName()));
        }
    }

    protected void readItemTag(@NotNull CompoundTag tag) {
        // Read plane's name
        CompoundTag displayTag = tag.getCompound("display");
        if (displayTag.contains("Name", Tag.TAG_STRING)) {
            setCustomName(Component.Serializer.fromJson(displayTag.getString("Name")));
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (getHealth() < 1.0f && (player.isShiftKeyDown() || !Config.getInstance().requireShiftForRepair) && !hasPassenger(player)) {
            if (!level().isClientSide) {
                player.causeFoodExhaustion(Config.getInstance().repairExhaustion);
                repair(Config.getInstance().repairSpeed);

                // Repair message
                MutableComponent component = Component.translatable("immersive_aircraft.repair", (int) (getHealth() * 100.0f));
                if (getHealth() < 0.33) {
                    component.withStyle(ChatFormatting.RED);
                } else if (getHealth() < 0.66) {
                    component.withStyle(ChatFormatting.GOLD);
                } else {
                    component.withStyle(ChatFormatting.GREEN);
                }
                player.displayClientMessage(component, true);

                level().playSound(null, getX(), getY(), getZ(), Sounds.REPAIR.get(), SoundSource.NEUTRAL, 1.0f, 0.7f + random.nextFloat() * 0.2f);
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
        if (!level().isClientSide) {
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
        if ((verticalCollision || horizontalCollision) && level().isClientSide && Config.getInstance().collisionDamage) {
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
    protected void checkFallDamage(double heightDifference, boolean onGround, @NotNull BlockState landedState, @NotNull BlockPos landedPosition) {

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
        return VehicleDataLoader.get(identifier).getBoundingBoxes().stream().map(this::getOffsetBoundingBox).toList();
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

    @Override
    public AABB getBoundingBoxForCulling() {
        AABB box = super.getBoundingBoxForCulling();
        for (AABB additionalShape : getAdditionalShapes()) {
            box = box.minmax(additionalShape);
        }
        return box;
    }

    public void setAnimationVariables(float tickDelta) {
        BBAnimationVariables.set("pressing_interpolated_x", pressingInterpolatedX.getSmooth(tickDelta));
        BBAnimationVariables.set("pressing_interpolated_y", pressingInterpolatedY.getSmooth(tickDelta));
        BBAnimationVariables.set("pressing_interpolated_z", pressingInterpolatedZ.getSmooth(tickDelta));

        Vec3 speed = getSpeedVector();
        BBAnimationVariables.set("velocity_x", (float) speed.x);
        BBAnimationVariables.set("velocity_y", (float) speed.y);
        BBAnimationVariables.set("velocity_z", (float) speed.z);
    }

    @Override
    public Component getDisplayName() {
        return super.getDisplayName();
    }
}