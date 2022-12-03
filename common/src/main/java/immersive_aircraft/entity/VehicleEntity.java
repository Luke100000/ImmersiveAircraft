package immersive_aircraft.entity;

import com.google.common.collect.Lists;
import immersive_aircraft.util.InterpolatedFloat;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract vehicle which handles player input, collisions, passengers and destruction
 */
public abstract class VehicleEntity extends Entity {
    static final TrackedData<Integer> DAMAGE_WOBBLE_TICKS = DataTracker.registerData(VehicleEntity.class, TrackedDataHandlerRegistry.INTEGER);
    static final TrackedData<Integer> DAMAGE_WOBBLE_SIDE = DataTracker.registerData(VehicleEntity.class, TrackedDataHandlerRegistry.INTEGER);
    static final TrackedData<Float> DAMAGE_WOBBLE_STRENGTH = DataTracker.registerData(VehicleEntity.class, TrackedDataHandlerRegistry.FLOAT);

    int interpolationSteps;

    double x;
    double y;
    double z;

    double clientYaw;
    double clientPitch;

    float movementX;
    float movementY;
    float movementZ;

    public final InterpolatedFloat pressingInterpolatedX;
    public final InterpolatedFloat pressingInterpolatedY;
    public final InterpolatedFloat pressingInterpolatedZ;

    double fallVelocity;

    public float roll;
    public float prevRoll;

    public float getRoll() {
        return roll;
    }

    public float getRoll(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevRoll, getRoll());
    }

    abstract protected List<List<Vec3d>> getPassengerPositions();

    protected int getPassengerSpace() {
        return getPassengerPositions().size();
    }

    @Override
    public void setPitch(float pitch) {
        float loops = (float)(Math.floor((pitch + 180f) / 360f) * 360f);
        pitch -= loops;
        prevPitch -= loops;
        super.setPitch(pitch);
    }

    public VehicleEntity(EntityType<? extends AircraftEntity> entityType, World world) {
        super(entityType, world);
        intersectionChecked = true;
        stepHeight = 0.55f;

        pressingInterpolatedX = new InterpolatedFloat(getInputInterpolationSteps());
        pressingInterpolatedY = new InterpolatedFloat(getInputInterpolationSteps());
        pressingInterpolatedZ = new InterpolatedFloat(getInputInterpolationSteps());
    }

    private float getInputInterpolationSteps() {
        return 10;
    }

    @Override
    protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height;
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    @Override
    protected void initDataTracker() {
        dataTracker.startTracking(DAMAGE_WOBBLE_TICKS, 0);
        dataTracker.startTracking(DAMAGE_WOBBLE_SIDE, 1);
        dataTracker.startTracking(DAMAGE_WOBBLE_STRENGTH, 0.0f);
    }

    @Override
    public boolean collidesWith(Entity other) {
        return AircraftEntity.canCollide(this, other);
    }

    public static boolean canCollide(Entity entity, Entity other) {
        return (other.isCollidable() || other.isPushable()) && !entity.isConnectedThroughVehicle(other);
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect) {
        return LivingEntity.positionInPortal(super.positionInPortal(portalAxis, portalRect));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        if (world.isClient || isRemoved()) {
            return true;
        }
        setDamageWobbleSide(-getDamageWobbleSide());
        setDamageWobbleTicks(10);
        setDamageWobbleStrength(getDamageWobbleStrength() + amount * 10.0f);
        emitGameEvent(GameEvent.ENTITY_DAMAGED, source.getAttacker());
        if (getDamageWobbleStrength() > 40.0f) {
            if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
                dropItem(asItem());
            }
            discard();
        }
        return true;
    }

    @Override
    public void onBubbleColumnSurfaceCollision(boolean drag) {
        world.addParticle(ParticleTypes.SPLASH, getX() + (double)random.nextFloat(), getY() + 0.7, getZ() + (double)random.nextFloat(), 0.0, 0.0, 0.0);
        if (random.nextInt(20) == 0) {
            world.playSound(getX(), getY(), getZ(), getSplashSound(), getSoundCategory(), 1.0f, 0.8f + 0.4f * random.nextFloat(), false);
        }
        emitGameEvent(GameEvent.SPLASH, getPrimaryPassenger());
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        if (entity instanceof AircraftEntity) {
            if (entity.getBoundingBox().minY < getBoundingBox().maxY) {
                super.pushAwayFrom(entity);
            }
        } else if (entity.getBoundingBox().minY <= getBoundingBox().minY) {
            super.pushAwayFrom(entity);
        }
    }

    public Item asItem() {
        return Items.DARK_OAK_BOAT;
    }

    @Override
    public void animateDamage() {
        setDamageWobbleSide(-getDamageWobbleSide());
        setDamageWobbleTicks(10);
        setDamageWobbleStrength(getDamageWobbleStrength() * 11.0f);
    }

    @Override
    public boolean collides() {
        return !isRemoved();
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
        this.x = x;
        this.y = y;
        this.z = z;
        clientYaw = yaw;
        clientPitch = pitch;
        this.interpolationSteps = 10;
    }

    @Override
    public Direction getMovementDirection() {
        return getHorizontalFacing().rotateYClockwise();
    }


    private static float getMovementMultiplier(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0f;
        }
        return positive ? 1.0f : -1.0f;
    }

    @Override
    public void tick() {
        // pilot
        if (world.isClient() && getPassengerList().size() > 0) {
            Entity entity = getPassengerList().get(0);
            if (entity instanceof ClientPlayerEntity player) {
                setInputs(player.input.movementSideways, getMovementMultiplier(player.input.jumping, player.input.sneaking), player.input.movementForward);
            }
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

        // if it's the right side, update the velocity
        if (isLogicalSideForUpdatingMovement()) {
            updateVelocity();
            if (world.isClient) {
                updateController();
            }
            move(MovementType.SELF, getVelocity());
        }

        checkBlockCollision();
        List<Entity> list = world.getOtherEntities(this, getBoundingBox().expand(0.2f, -0.01f, 0.2f), EntityPredicates.canBePushedBy(this));
        if (!list.isEmpty()) {
            boolean bl = !world.isClient && !(getPrimaryPassenger() instanceof PlayerEntity);
            for (Entity entity : list) {
                if (entity.hasPassenger(this)) continue;
                if (bl && getPassengerList().size() < 2 && !entity.hasVehicle() && entity.getWidth() < getWidth() && entity instanceof LivingEntity && !(entity instanceof WaterCreatureEntity) && !(entity instanceof PlayerEntity)) {
                    entity.startRiding(this);
                    continue;
                }
                pushAwayFrom(entity);
            }
        }

        // interpolate keys for visual feedback
        if (world.isClient) {
            pressingInterpolatedX.update(movementX);
            pressingInterpolatedY.update(movementY);
            pressingInterpolatedZ.update(movementZ);
        }
    }

    private void handleClientSync() {
        if (isLogicalSideForUpdatingMovement()) {
            interpolationSteps = 0;
            updateTrackedPosition(getX(), getY(), getZ());
        }
        if (interpolationSteps <= 0) {
            return;
        }
        double interpolatedX = getX() + (x - getX()) / (double)interpolationSteps;
        double interpolatedY = getY() + (y - getY()) / (double)interpolationSteps;
        double interpolatedZ = getZ() + (z - getZ()) / (double)interpolationSteps;
        double interpolatedYaw = MathHelper.wrapDegrees(clientYaw - (double)getYaw());
        setYaw(getYaw() + (float)interpolatedYaw / (float)interpolationSteps);
        setPitch(getPitch() + (float)(clientPitch - (double)getPitch()) / (float)interpolationSteps);

        setPosition(interpolatedX, interpolatedY, interpolatedZ);
        setRotation(getYaw(), getPitch());

        --interpolationSteps;
    }

    public float method_7544() {
        Box box = getBoundingBox();
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.ceil(box.maxX);
        int k = MathHelper.floor(box.maxY);
        int l = MathHelper.ceil(box.maxY - fallVelocity);
        int m = MathHelper.floor(box.minZ);
        int n = MathHelper.ceil(box.maxZ);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        block0:
        for (int o = k; o < l; ++o) {
            float f = 0.0f;
            for (int p = i; p < j; ++p) {
                for (int q = m; q < n; ++q) {
                    mutable.set(p, o, q);
                    FluidState fluidState = world.getFluidState(mutable);
                    if (fluidState.isIn(FluidTags.WATER)) {
                        f = Math.max(f, fluidState.getHeight(world, mutable));
                    }
                    if (f >= 1.0f) continue block0;
                }
            }
            if (!(f < 1.0f)) continue;
            return (float)mutable.getY() + f;
        }
        return l + 1;
    }

    abstract void updateVelocity();

    protected float getGravity() {
        return -0.04f;
    }

    abstract void updateController();

    @Override
    public void updatePassengerPosition(Entity passenger) {
        if (!hasPassenger(passenger)) {
            return;
        }

        Matrix4f transform = getVehicleTransform();

        int size = getPassengerList().size() - 1;
        List<List<Vec3d>> positions = getPassengerPositions();
        if (size < positions.size()) {
            int i = getPassengerList().indexOf(passenger);
            if (i >= 0 && i < positions.get(size).size()) {
                Vec3d position = positions.get(size).get(i);

                //animals are thicc
                if (passenger instanceof AnimalEntity) {
                    position.add(0.0f, 0.0f, 0.2f);
                }

                position = position.add(0, -passenger.getHeightOffset(), 0);

                Vector4f worldPosition = transformPosition(transform, (float)position.x, (float)position.y, (float)position.z);

                passenger.setPosition(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());

                passenger.setYaw(passenger.getYaw() + (getYaw() - prevYaw));
                passenger.setHeadYaw(passenger.getHeadYaw() + (getYaw() - prevYaw));

                copyEntityData(passenger);
                if (passenger instanceof AnimalEntity && size > 1) {
                    int angle = passenger.getId() % 2 == 0 ? 90 : 270;
                    passenger.setBodyYaw(((AnimalEntity)passenger).bodyYaw + (float)angle);
                    passenger.setHeadYaw(passenger.getHeadYaw() + (float)angle);
                }
            }
        }
    }

    private Vec3d getDismountOffset(double vehicleWidth, double passengerWidth) {
        double d = (vehicleWidth + passengerWidth + (double)1.0E-5f) / 2.0;
        float yaw = getYaw() + 90.0f;
        float f = -MathHelper.sin(yaw * ((float)Math.PI / 180));
        float g = MathHelper.cos(yaw * ((float)Math.PI / 180));
        float h = Math.max(Math.abs(f), Math.abs(g));
        return new Vec3d((double)f * d / (double)h, 0.0, (double)g * d / (double)h);
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        if (getVelocity().lengthSquared() < 0.1f) {
            double e;
            Vec3d vec3d = getDismountOffset(getWidth() * MathHelper.SQUARE_ROOT_OF_TWO, passenger.getWidth());
            double d = getX() + vec3d.x;
            BlockPos blockPos = new BlockPos(d, getBoundingBox().maxY, e = getZ() + vec3d.z);
            BlockPos blockPos2 = blockPos.down();
            if (!world.isWater(blockPos2)) {
                double g;
                ArrayList<Vec3d> list = Lists.newArrayList();
                double f = world.getDismountHeight(blockPos);
                if (Dismounting.canDismountInBlock(f)) {
                    list.add(new Vec3d(d, (double)blockPos.getY() + f, e));
                }
                if (Dismounting.canDismountInBlock(g = world.getDismountHeight(blockPos2))) {
                    list.add(new Vec3d(d, (double)blockPos2.getY() + g, e));
                }
                for (EntityPose entityPose : passenger.getPoses()) {
                    for (Vec3d vec3d2 : list) {
                        if (!Dismounting.canPlaceEntityAt(world, vec3d2, passenger, entityPose)) continue;
                        passenger.setPose(entityPose);
                        return vec3d2;
                    }
                }
            }
        }
        return super.updatePassengerForDismount(passenger);
    }

    protected void copyEntityData(Entity entity) {
        entity.setBodyYaw(getYaw());
        float f = MathHelper.wrapDegrees(entity.getYaw() - getYaw());
        float g = MathHelper.clamp(f, -105.0f, 105.0f);
        entity.prevYaw += g - f;
        entity.setYaw(entity.getYaw() + g - f);
        entity.setHeadYaw(entity.getYaw());
    }

    @Override
    public void onPassengerLookAround(Entity passenger) {
        copyEntityData(passenger);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if (player.shouldCancelInteraction()) {
            return ActionResult.PASS;
        }
        if (!world.isClient) {
            return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void move(MovementType movementType, Vec3d movement) {
        Vec3d prediction = getPos().add(movement);
        super.move(movementType, movement);

        if (verticalCollision || horizontalCollision) {
            float collision = (float)(prediction.subtract(getPos()).length() - Math.abs(getGravity()));
            if (collision > 0.0001f) {
                float repeat = 1.0f - (getDamageWobbleTicks() + 1) / 10.0f;
                if (repeat > 0.0f) {
                    setDamageWobbleSide(-getDamageWobbleSide());
                    setDamageWobbleTicks(10);
                    setDamageWobbleStrength(getDamageWobbleStrength() + collision * 50 * repeat * repeat);
                }
            }
        }
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {

    }

    public void setDamageWobbleStrength(float wobbleStrength) {
        dataTracker.set(DAMAGE_WOBBLE_STRENGTH, wobbleStrength);
    }

    public float getDamageWobbleStrength() {
        return dataTracker.get(DAMAGE_WOBBLE_STRENGTH);
    }

    public void setDamageWobbleTicks(int wobbleTicks) {
        dataTracker.set(DAMAGE_WOBBLE_TICKS, wobbleTicks);
    }

    public int getDamageWobbleTicks() {
        return dataTracker.get(DAMAGE_WOBBLE_TICKS);
    }

    public void setDamageWobbleSide(int side) {
        dataTracker.set(DAMAGE_WOBBLE_SIDE, side);
    }

    public int getDamageWobbleSide() {
        return dataTracker.get(DAMAGE_WOBBLE_SIDE);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengerList().size() < getPassengerSpace() && !isSubmergedIn(FluidTags.WATER);
    }

    @Override
    @Nullable
    public Entity getPrimaryPassenger() {
        return getFirstPassenger();
    }

    public void setInputs(float x, float y, float z) {
        this.movementX = x;
        this.movementY = y;
        this.movementZ = z;
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(asItem());
    }

    public boolean isWithinParticleRange() {
        return MinecraftClient.getInstance().gameRenderer.getCamera().getPos().squaredDistanceTo(getPos()) < 1024;
    }

    protected Vector4f transformPosition(Matrix4f transform, float x, float y, float z) {
        Vector4f p0 = new Vector4f(x, y, z, 1);
        p0.transform(transform);
        return p0;
    }

    protected Vec3f transformVector(float x, float y, float z) {
        return transformVector(getVehicleNormalTransform(), x, y, z);
    }

    protected Vec3f transformVector(Matrix3f transform, float x, float y, float z) {
        Vec3f p0 = new Vec3f(x, y, z);
        p0.transform(transform);
        return p0;
    }

    protected Matrix4f getVehicleTransform() {
        Matrix4f transform = Matrix4f.translate((float)getX(), (float)getY(), (float)getZ());
        transform.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-getYaw()));
        transform.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(getPitch()));
        transform.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(getRoll()));
        return transform;
    }

    protected Matrix3f getVehicleNormalTransform() {
        Matrix3f transform = Matrix3f.scale(1.0f, 1.0f, 1.0f);
        transform.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-getYaw()));
        transform.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(getPitch()));
        transform.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(getRoll()));
        return transform;
    }

    public Vec3d getDirection() {
        Vec3f f = transformVector(0.0f, 0.0f, 1.0f);
        return new Vec3d(f.getX(), f.getY(), f.getZ());
    }

    public Vec3d getTopDirection() {
        Vec3f f = transformVector(0.0f, 1.0f, 0.0f);
        return new Vec3d(f.getX(), f.getY(), f.getZ());
    }

    protected final static Vector4f ZERO_VEC4 = new Vector4f();
}