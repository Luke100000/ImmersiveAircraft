package immersive_aircraft.entity;

import immersive_aircraft.client.KeyBindings;
import immersive_aircraft.cobalt.network.NetworkHandler;
import immersive_aircraft.compat.Matrix3f;
import immersive_aircraft.compat.Matrix4f;
import immersive_aircraft.compat.Vec3f;
import immersive_aircraft.compat.Vector4f;
import immersive_aircraft.config.Config;
import immersive_aircraft.network.c2s.CollisionMessage;
import immersive_aircraft.network.c2s.CommandMessage;
import immersive_aircraft.util.InterpolatedFloat;
import immersive_aircraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Abstract vehicle which handles player input, collisions, passengers and destruction
 *
 * 1.12.2 port notes:
 *  - DataTracker -> EntityDataManager, tick() -> onUpdate(), motion via motionX/Y/Z
 *  - the custom portal / dismount-position logic of the 1.16 code has no 1.12 hook and
 *    was dropped (vanilla 1.12 dismount placement applies) // TODO(port) if needed
 */
public abstract class VehicleEntity extends Entity {
	protected static final DataParameter<Integer> DAMAGE_WOBBLE_TICKS = EntityDataManager.createKey(VehicleEntity.class, DataSerializers.VARINT);
	protected static final DataParameter<Integer> DAMAGE_WOBBLE_SIDE = EntityDataManager.createKey(VehicleEntity.class, DataSerializers.VARINT);
	protected static final DataParameter<Float> DAMAGE_WOBBLE_STRENGTH = EntityDataManager.createKey(VehicleEntity.class, DataSerializers.FLOAT);

	protected static final DataParameter<Integer> BOOST = EntityDataManager.createKey(VehicleEntity.class, DataSerializers.VARINT);

	protected int interpolationSteps;

	protected double x;
	protected double y;
	protected double z;

	protected double clientYaw;
	protected double clientPitch;

	protected float movementX;
	protected float movementY;
	protected float movementZ;

    public final InterpolatedFloat pressingInterpolatedX;
    public final InterpolatedFloat pressingInterpolatedY;
    public final InterpolatedFloat pressingInterpolatedZ;

    public float roll;
    public float prevRoll;

    public float getRoll() {
        return roll;
    }

    public float getRoll(float tickDelta) {
        return Utils.lerp(tickDelta, prevRoll, getRoll());
    }

    public void boost() {
        dataManager.set(BOOST, 100);
    }

    protected void applyBoost() {

    }

    public boolean canBoost() {
        return false;
    }

    public int getBoost() {
        return dataManager.get(BOOST);
    }

    abstract protected List<List<Vec3d>> getPassengerPositions();

    protected int getPassengerSpace() {
        return getPassengerPositions().size();
    }

    public void setPitch(float pitch) {
        float loops = (float) (Math.floor((pitch + 180f) / 360f) * 360f);
        pitch -= loops;
        prevRotationPitch -= loops;
        this.rotationPitch = pitch;
    }

    public VehicleEntity(World world) {
        super(world);
        stepHeight = 0.55f;

        pressingInterpolatedX = new InterpolatedFloat(getInputInterpolationSteps());
        pressingInterpolatedY = new InterpolatedFloat(getInputInterpolationSteps());
        pressingInterpolatedZ = new InterpolatedFloat(getInputInterpolationSteps());
    }

    protected float getInputInterpolationSteps() {
        return 10;
    }

    @Override
    public float getEyeHeight() {
        return this.height;
    }

    @Override
    protected void entityInit() {
        dataManager.register(DAMAGE_WOBBLE_TICKS, 0);
        dataManager.register(DAMAGE_WOBBLE_SIDE, 1);
        dataManager.register(DAMAGE_WOBBLE_STRENGTH, 0.0f);
        dataManager.register(BOOST, 0);
    }

    public static boolean canCollide(Entity entity, Entity other) {
        return (other.canBeCollidedWith() || other.canBePushed()) && !entity.isRidingSameEntity(other);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canBePushed() {
        return true;
    }

    @Override
    public boolean hitByEntity(Entity attacker) {
        return isPassenger(attacker) || super.hitByEntity(attacker);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable(source)) {
            return false;
        }
        if (world.isRemote || isDead) {
            return true;
        }
        setDamageWobbleSide(-getDamageWobbleSide());
        setDamageWobbleTicks(10);
        setDamageWobbleStrength(getDamageWobbleStrength() + amount * 10.0f / getDurability());
        boolean bl = source.getTrueSource() instanceof EntityPlayer && ((EntityPlayer)source.getTrueSource()).capabilities.isCreativeMode;
        if (bl || getDamageWobbleStrength() > 40.0f) {
            if (!Config.getInstance().onlyPlayerCanDestroyAircraft || isBeingRidden() || source.getTrueSource() instanceof EntityPlayer) {
                if (world.getGameRules().getBoolean("doEntityDrops")) {
                    drop();
                }
                setDead();
            }
        }
        return true;
    }

    protected float getDurability() {
        return 1.0f;
    }

    protected void drop() {
        entityDropItem(createDropStack(), 0.0f);
    }

    protected net.minecraft.item.ItemStack createDropStack() {
        return new net.minecraft.item.ItemStack(asItem());
    }

    @Override
    public void applyEntityCollision(Entity entity) {
        if (entity instanceof AircraftEntity) {
            if (entity.getEntityBoundingBox().minY < getEntityBoundingBox().maxY) {
                super.applyEntityCollision(entity);
            }
        } else if (entity.getEntityBoundingBox().minY <= getEntityBoundingBox().minY) {
            super.applyEntityCollision(entity);
        }
    }

    public Item asItem() {
        return Items.DARK_OAK_BOAT;
    }

    @Override
    public void performHurtAnimation() {
        setDamageWobbleSide(-getDamageWobbleSide());
        setDamageWobbleTicks(10);
        setDamageWobbleStrength(getDamageWobbleStrength() * 11.0f);
    }

    @Override
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        this.x = x;
        this.y = y;
        this.z = z;
        clientYaw = yaw;
        clientPitch = pitch;
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
    public void onUpdate() {
        // pilot
        if (world.isRemote && getPassengers().size() > 0) {
            for (Entity entity : getPassengers()) {
                if (entity instanceof EntityPlayerSP) {
                    if (KeyBindings.dismount.wasPressed()) {
                        NetworkHandler.sendToServer(new CommandMessage(CommandMessage.Key.DISMOUNT, getVelocity()));
                    }
                    if (KeyBindings.boost.wasPressed() && canBoost()) {
                        NetworkHandler.sendToServer(new CommandMessage(CommandMessage.Key.BOOST, getVelocity()));
                        Vec3d p = getPositionVector();
                        world.playSound(p.x, p.y, p.z, SoundEvents.ENTITY_FIREWORK_LAUNCH, SoundCategory.NEUTRAL, 1.0f, 1.0f, true);
                    }
                }
            }

            //controls
            Entity pilot = getPassengers().get(0);
            if (pilot instanceof EntityPlayerSP) {
                setInputs(getMovementMultiplier(
                                KeyBindings.left.isPressed(),
                                KeyBindings.right.isPressed()
                        ), getMovementMultiplier(
                                KeyBindings.up.isPressed(),
                                KeyBindings.down.isPressed()
                        ),
                        getMovementMultiplier(
                                useAirplaneControls() ? KeyBindings.push.isPressed() : KeyBindings.forward.isPressed(),
                                useAirplaneControls() ? KeyBindings.pull.isPressed() : KeyBindings.backward.isPressed()
                        )
                );
            }
        }

        // wobble
        if (getDamageWobbleTicks() > 0) {
            setDamageWobbleTicks(getDamageWobbleTicks() - 1);
        }
        if (getDamageWobbleStrength() > 0.0f) {
            setDamageWobbleStrength(getDamageWobbleStrength() - 1.0f);
        }

        super.onUpdate();

        // interpolate
        handleClientSync();


        int boost = getBoost();
        if (boost > 0) {
            dataManager.set(BOOST, boost - 1);
        }

        // if it's the right side, update the velocity
        if (isLogicalSideForUpdatingMovement()) {
            updateVelocity();

            // boost
            if (boost > 0) {
                applyBoost();
            }

            if (world.isRemote) {
                updateController();
            }

            move(MoverType.SELF, motionX, motionY, motionZ);
        }

        // auto enter
        doBlockCollisions();
        List<Entity> list = world.getEntitiesInAABBexcluding(this, getEntityBoundingBox().grow(0.2f, -0.01f, 0.2f), entity -> !isRidingSameEntity(entity));
        if (!list.isEmpty()) {
            boolean bl = !world.isRemote && !(getControllingPassenger() instanceof EntityPlayer);
            for (Entity entity : list) {
                if (entity.isPassenger(this)) continue;
                if (bl && getPassengers().size() < (getPassengerSpace() - 1) && !entity.isRiding() && entity.width < width && entity instanceof EntityLivingBase && !(entity instanceof EntityWaterMob) && !(entity instanceof EntityPlayer)) {
                    entity.startRiding(this);
                    continue;
                }
                applyEntityCollision(entity);
            }
        }

        // interpolate keys for visual feedback
        if (world.isRemote) {
            pressingInterpolatedX.update(movementX);
            pressingInterpolatedY.update(movementY);
            pressingInterpolatedZ.update(movementZ);
        }
    }

    private void handleClientSync() {
        if (isLogicalSideForUpdatingMovement()) {
            interpolationSteps = 0;
        }
        if (interpolationSteps <= 0) {
            return;
        }
        double interpolatedX = posX + (x - posX) / (double) interpolationSteps;
        double interpolatedY = posY + (y - posY) / (double) interpolationSteps;
        double interpolatedZ = posZ + (z - posZ) / (double) interpolationSteps;
        double interpolatedYaw = MathHelper.wrapDegrees(clientYaw - getYaw());
        setYaw(getYaw() + (float) interpolatedYaw / (float) interpolationSteps);
        setPitch(getPitch() + (float) (clientPitch - (double) getPitch()) / (float) interpolationSteps);

        setPosition(interpolatedX, interpolatedY, interpolatedZ);
        setRotation(getYaw(), getPitch());

        --interpolationSteps;
    }

    /**
     * 1.16.5 {@code Entity.isLogicalSideForUpdatingMovement}: a vehicle steered by a player is
     * simulated by that player's client only, the server takes its position from
     * CPacketVehicleMove. Simulating on both sides makes the server broadcast its own
     * (thrustless, gravity-only) motion back through SPacketEntityVelocity, which resets the
     * pilot's motion every few ticks - planes then never build up the speed needed for lift.
     * <p>
     * 1.12.2 already has exactly this predicate as {@link Entity#canPassengerSteer()}, and it is
     * the same one EntityPlayerSP uses to decide whether to send CPacketVehicleMove, so delegate
     * to it to keep simulation and position reporting on the same side.
     */
    public boolean isLogicalSideForUpdatingMovement() {
        return canPassengerSteer();
    }

    protected abstract void updateVelocity();

    protected float getGravity() {
        return -0.04f;
    }

    protected abstract void updateController();

    @Override
    public void updatePassenger(Entity passenger) {
        if (!isPassenger(passenger)) {
            return;
        }

        Matrix4f transform = getVehicleTransform();

        int size = getPassengers().size() - 1;
        List<List<Vec3d>> positions = getPassengerPositions();
        if (size < positions.size()) {
            int i = getPassengers().indexOf(passenger);
            if (i >= 0 && i < positions.get(size).size()) {
                Vec3d position = positions.get(size).get(i);

                //animals are thicc
                if (passenger instanceof EntityAnimal) {
                    position.add(0.0f, 0.0f, 0.2f);
                }

                position = position.add(0, passenger.getYOffset(), 0);

                Vector4f worldPosition = transformPosition(transform, (float) position.x, (float) position.y, (float) position.z);

                passenger.setPosition(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());

                passenger.rotationYaw = passenger.rotationYaw + (getYaw() - prevRotationYaw);
                passenger.setRotationYawHead(passenger.getRotationYawHead() + (getYaw() - prevRotationYaw));

                copyEntityData(passenger);
                if (passenger instanceof EntityLivingBase && size > 1) {
                    int angle = passenger.getEntityId() % 2 == 0 ? 90 : 270;
                    EntityLivingBase living = (EntityLivingBase) passenger;
                    living.renderYawOffset += (float) angle;
                    passenger.setRotationYawHead(passenger.getRotationYawHead() + (float) angle);
                }
            }
        }
    }

    protected void copyEntityData(Entity entity) {
        if (entity instanceof EntityLivingBase) {
            ((EntityLivingBase) entity).renderYawOffset = getYaw();
        }
        float f = MathHelper.wrapDegrees(entity.rotationYaw - getYaw());
        float g = MathHelper.clamp(f, -105.0f, 105.0f);
        entity.prevRotationYaw += g - f;
        entity.rotationYaw = entity.rotationYaw + g - f;
        entity.setRotationYawHead(entity.rotationYaw);
    }

    @Override
    public void applyOrientationToEntity(Entity entityToUpdate) {
        copyEntityData(entityToUpdate);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {

    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (player.isSneaking()) {
            return false;
        }
        if (isPassenger(player)) {
            return false;
        }
        if (!world.isRemote && !player.isRiding()) {
            player.startRiding(this);
        }
        return true;
    }

    @Override
    public void move(MoverType movementType, double mx, double my, double mz) {
        Vec3d prediction = getPositionVector().add(mx, my, mz);
        super.move(movementType, mx, my, mz);

        // Collision damage
        if (world.isRemote && Config.getInstance().collisionDamage) {
            if (collidedVertically || collidedHorizontally) {
                double maxPossibleError = Math.sqrt(mx * mx + my * my + mz * mz);
                double error = prediction.distanceTo(getPositionVector());
                if (error <= maxPossibleError) {
                    float collision = (float) (error - (collidedVertically ? Math.abs(getGravity()) : 0.0));
                    if (collision > 0.01f) {
                        float repeat = 1.0f - (getDamageWobbleTicks() + 1) / 10.0f;
                        if (repeat > 0.0001f) {
                            float damage = collision * 20 * repeat * repeat;
                            NetworkHandler.sendToServer(new CollisionMessage(damage));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void fall(float distance, float damageMultiplier) {

    }

    public void setDamageWobbleStrength(float wobbleStrength) {
        dataManager.set(DAMAGE_WOBBLE_STRENGTH, wobbleStrength);
    }

    public float getDamageWobbleStrength() {
        return dataManager.get(DAMAGE_WOBBLE_STRENGTH);
    }

    public void setDamageWobbleTicks(int wobbleTicks) {
        dataManager.set(DAMAGE_WOBBLE_TICKS, wobbleTicks);
    }

    public int getDamageWobbleTicks() {
        return dataManager.get(DAMAGE_WOBBLE_TICKS);
    }

    public void setDamageWobbleSide(int side) {
        dataManager.set(DAMAGE_WOBBLE_SIDE, side);
    }

    public int getDamageWobbleSide() {
        return dataManager.get(DAMAGE_WOBBLE_SIDE);
    }

    @Override
    protected boolean canFitPassenger(Entity passenger) {
        return getPassengers().size() < getPassengerSpace() && !isInsideOfMaterial(net.minecraft.block.material.Material.WATER);
    }

    @Override
    @Nullable
    public Entity getControllingPassenger() {
        return getFirstPassenger();
    }

    @Nullable
    public Entity getGunner(int offset) {
        List<Entity> passengers = getPassengers();
        return passengers.isEmpty() ? null : passengers.get(Math.max(0, passengers.size() - 1 - offset));
    }

    public boolean isPilotCreative() {
        return getControllingPassenger() instanceof EntityPlayer && ((EntityPlayer) getControllingPassenger()).isCreative();
    }

    public void setInputs(float x, float y, float z) {
        this.movementX = x;
        this.movementY = y;
        this.movementZ = z;
    }

    public boolean isWithinParticleRange() {
        net.minecraft.entity.Entity view = Minecraft.getMinecraft().getRenderViewEntity();
        return view != null && view.getPositionVector().squareDistanceTo(getPositionVector()) < 1024;
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

    public Matrix4f getVehicleTransform() {
        Matrix4f transform = Matrix4f.translate((float) posX, (float) posY, (float) posZ);
        transform.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-getYaw()));
        transform.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(getPitch()));
        transform.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(getRoll()));
        return transform;
    }

    public Matrix3f getVehicleNormalTransform() {
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

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        double d = Config.getInstance().renderDistance;
        return distance < d * d;
    }

    public void chill() {

    }

    public double getZoom() {
        return 0.0;
    }

    // Start of compat

    public float getYaw() {
        return rotationYaw;
    }

    public void setYaw(float yaw) {
        this.rotationYaw = yaw;
    }

    public float getPitch() {
        return rotationPitch;
    }

    public float getPitch(float tickDelta) {
        return prevRotationPitch + (rotationPitch - prevRotationPitch) * tickDelta;
    }

    public float getYaw(float tickDelta) {
        return prevRotationYaw + (rotationYaw - prevRotationYaw) * tickDelta;
    }

    @Nullable
    public Entity getFirstPassenger() {
        List<Entity> list = this.getPassengers();
        return list.isEmpty() ? null : list.get(0);
    }

    public Vec3d getVelocity() {
        return new Vec3d(motionX, motionY, motionZ);
    }

    public void setVelocity(Vec3d velocity) {
        motionX = velocity.x;
        motionY = velocity.y;
        motionZ = velocity.z;
    }

    public double getX() {
        return posX;
    }

    public double getY() {
        return posY;
    }

    public double getZ() {
        return posZ;
    }
}
