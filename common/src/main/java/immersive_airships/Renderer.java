package immersive_airships;

import immersive_airships.client.render.entity.renderer.GyrodyneEntityRenderer;
import immersive_airships.cobalt.registration.Registration;

public class Renderer {
    public static void bootstrap() {
        Registration.registerEntityRenderer(Entities.GYRODYNE, GyrodyneEntityRenderer::new);
    }
}
