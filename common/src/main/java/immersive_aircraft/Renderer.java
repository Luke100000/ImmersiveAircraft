package immersive_aircraft;

import immersive_aircraft.client.render.entity.renderer.*;
import immersive_aircraft.client.render.entity.renderer.bullet.BulletEntityRenderer;
import immersive_aircraft.cobalt.registration.Registration;

public class Renderer {
    public static void bootstrap() {
        Registration.register(Entities.GYRODYNE.get(), GyrodyneEntityRenderer::new);
        Registration.register(Entities.BIPLANE.get(), BiplaneEntityRenderer::new);
        Registration.register(Entities.AIRSHIP.get(), AirshipEntityRenderer::new);
        Registration.register(Entities.CARGO_AIRSHIP.get(), CargoAirshipEntityRenderer::new);
        Registration.register(Entities.QUADROCOPTER.get(), QuadrocopterEntityRenderer::new);
        Registration.register(Entities.BLIMP.get(), BlimpEntityRenderer::new);

        Registration.register(Entities.BULLET.get(), BulletEntityRenderer::new);
    }
}