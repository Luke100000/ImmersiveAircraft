package immersive_aircraft;

import immersive_aircraft.client.render.entity.renderer.*;
import immersive_aircraft.client.render.entity.renderer.bullet.*;
import immersive_aircraft.cobalt.registration.ClientRegistration;

public class Renderer {
    public static void bootstrap() {
        ClientRegistration.register(Entities.GYRODYNE.get(), GyrodyneEntityRenderer::new);
        ClientRegistration.register(Entities.BIPLANE.get(), BiplaneEntityRenderer::new);
        ClientRegistration.register(Entities.AIRSHIP.get(), AirshipEntityRenderer::new);
        ClientRegistration.register(Entities.CARGO_AIRSHIP.get(), CargoAirshipEntityRenderer::new);
        ClientRegistration.register(Entities.WARSHIP.get(), WarshipEntityRenderer::new);
        ClientRegistration.register(Entities.QUADROCOPTER.get(), QuadrocopterEntityRenderer::new);
        ClientRegistration.register(Entities.BAMBOO_HOPPER.get(), BambooHopperEntityRenderer::new);

        ClientRegistration.register(Entities.BULLET.get(), BulletEntityRenderer::new);
        ClientRegistration.register(Entities.TINY_TNT.get(), TinyTNTRenderer::new);
    }
}
