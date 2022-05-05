package immersive_airships.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import immersive_airships.entity.BiplaneEntity;
import net.minecraft.client.model.*;

public class BiplaneEntityModel<T extends BiplaneEntity> extends AirshipEntityModel<T> {
    private final ModelPart propeller;

    final ImmutableList<ModelPart> parts;

    public BiplaneEntityModel(ModelPart root) {
        super();

        propeller = root.getChild("propeller");

        this.parts = ImmutableList.of(
                root.getChild("body"),
                root.getChild("propeller")
        );
    }

    @Override
    public void setAngles(T entity, float f, float g, float h, float i, float j) {
        super.setAngles(entity, f, g, h, i, j);

        propeller.pitch += entity.getEnginePower();
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        ModelPartData body = modelPartData.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(3.0F, -2.0F, -48.0F, 16.0F, 1.0F, 96.0F)
                .uv(0, 0).cuboid(3.0F, -10.0F, -48.0F, 16.0F, 1.0F, 96.0F)
                .uv(0, 0).cuboid(-20.0F, -1.0F, -8.0F, 23.0F, 1.0F, 16.0F)
                .uv(0, 0).cuboid(-30.0F, -10.0F, -8.0F, 10.0F, 10.0F, 16.0F)
                .uv(0, 0).cuboid(-51.0F, -9.0F, -3.0F, 21.0F, 8.0F, 6.0F)
                .uv(0, 0).cuboid(-52.0F, -5.0F, -19.0F, 11.0F, 1.0F, 38.0F)
                .uv(0, 0).cuboid(-54.0F, -19.0F, -1.0F, 13.0F, 17.0F, 2.0F)
                .uv(0, 0).cuboid(1.0F, -11.0F, -4.0F, 20.0F, 11.0F, 8.0F)
                .uv(0, 0).cuboid(-20.0F, -7.0F, 7.0F, 23.0F, 6.0F, 1.0F)
                .uv(0, 0).cuboid(-20.0F, -7.0F, -8.0F, 23.0F, 6.0F, 1.0F), ModelTransform.pivot(0.0F, 12.0F, 0.0F));

        ModelPartData propeller = modelPartData.addChild("propeller", ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, -14.0F, -10.0F, 0.0F, 4.0F, 20.0F), ModelTransform.pivot(23.0F, 19.0F, 0.0F));

        return TexturedModelData.of(modelData, 256, 256);
    }

    @Override
    public Iterable<ModelPart> getParts() {
        return parts;
    }
}
