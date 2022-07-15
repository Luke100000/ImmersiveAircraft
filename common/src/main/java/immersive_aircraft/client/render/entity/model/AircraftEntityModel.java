package immersive_aircraft.client.render.entity.model;

import immersive_aircraft.entity.AircraftEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.model.CompositeEntityModel;

import java.util.Collections;

public abstract class AircraftEntityModel<T extends AircraftEntity> extends CompositeEntityModel<T> {
    boolean firstPerson;

    @Override
    public void setAngles(T aircraftEntity, float f, float g, float h, float i, float j) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        firstPerson = player != null && player.getRootVehicle() == aircraftEntity && !MinecraftClient.getInstance().gameRenderer.getCamera().isThirdPerson();
    }

    public Iterable<ModelPart> getBannerParts() {
        return Collections.emptyList();
    }
}

