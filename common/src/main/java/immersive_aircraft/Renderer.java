package immersive_aircraft;

import immersive_aircraft.client.render.entity.renderer.AirshipEntityRenderer;
import immersive_aircraft.client.render.entity.renderer.BiplaneEntityRenderer;
import immersive_aircraft.client.render.entity.renderer.GyrodyneEntityRenderer;
import immersive_aircraft.client.render.entity.renderer.QuadrocopterEntityRenderer;
import immersive_aircraft.cobalt.registration.Registration;

public class Renderer {
    public static void bootstrap() {
        Registration.register(Entities.GYRODYNE.get(), GyrodyneEntityRenderer::new);
        Registration.register(Entities.BIPLANE.get(), BiplaneEntityRenderer::new);
        Registration.register(Entities.AIRSHIP.get(), AirshipEntityRenderer::new);
        Registration.register(Entities.QUADROCOPTER.get(), QuadrocopterEntityRenderer::new);
    }
}