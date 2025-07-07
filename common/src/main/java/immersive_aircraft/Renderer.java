package immersive_aircraft;

import immersive_aircraft.client.render.entity.renderer.*;
import immersive_aircraft.client.render.entity.renderer.bullet.*;
import immersive_aircraft.cobalt.registration.Registration;

public class Renderer {
    public static void bootstrap() {
        Registration.register(Entities.GYRODYNE.get(), GyrodyneEntityRenderer::new);
        Registration.register(Entities.BIPLANE.get(), BiplaneEntityRenderer::new);
        Registration.register(Entities.AIRSHIP.get(), AirshipEntityRenderer::new);
        Registration.register(Entities.CARGO_AIRSHIP.get(), CargoAirshipEntityRenderer::new);
        Registration.register(Entities.WARSHIP.get(), WarshipEntityRenderer::new);
        Registration.register(Entities.QUADROCOPTER.get(), QuadrocopterEntityRenderer::new);
        Registration.register(Entities.BAMBOO_HOPPER.get(), BambooHopperEntityRenderer::new);

        Registration.register(Entities.BULLET.get(), BulletEntityRenderer::new);
        Registration.register(Entities.TINY_TNT.get(), TinyTNTRenderer::new);
    }
}