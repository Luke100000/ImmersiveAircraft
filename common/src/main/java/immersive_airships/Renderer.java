package immersive_airships;

import immersive_airships.client.render.entity.renderer.BiplaneEntityRenderer;
import immersive_airships.client.render.entity.renderer.GyrodyneEntityRenderer;
import immersive_airships.cobalt.registration.Registration;

public class Renderer {
    public static void bootstrap() {
        Registration.register(Entities.GYRODYNE, GyrodyneEntityRenderer::new);
        Registration.register(Entities.BIPLANE, BiplaneEntityRenderer::new);
    }
}
