package immersive_aircraft.client.render.entity.renderer;

import immersive_aircraft.resources.bbmodel.BBAnimationVariables;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.mariuszgromada.math.mxparser.Argument;

import java.util.*;

@Environment(EnvType.CLIENT)
public class VehicleEntityRenderState extends EntityRenderState {
    public float xRot;
    public float yRot;
    public float zRot;
    public Vec3 speedVector;
    public float damageWobbleTicks;
    public float damageWobbleStrength;
    public int damageWobbleSide;
    public float health;
    public final BBAnimationVariables animationVariables;
    public float time;
    public boolean isWithinParticleRange;
    public int packedLight;
    public Set<Entity> passengers;
    public LivingEntity controllingPassenger;
    public boolean onGround;
    public List<AABB> additionalShapes;

    public VehicleEntityRenderState() {
        animationVariables = new BBAnimationVariables();
        passengers = new HashSet<>();
        additionalShapes = new ArrayList<>();
    }
}
