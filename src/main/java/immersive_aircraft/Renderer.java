package immersive_aircraft;

import immersive_aircraft.client.render.entity.renderer.*;
import immersive_aircraft.entity.*;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class Renderer {
    public static void bootstrap() {
        RenderingRegistry.registerEntityRenderingHandler(GyrodyneEntity.class, GyrodyneEntityRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(BiplaneEntity.class, BiplaneEntityRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(AirshipEntity.class, AirshipEntityRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(CargoAirshipEntity.class, CargoAirshipEntityRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(QuadrocopterEntity.class, QuadrocopterEntityRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(WarshipEntity.class, WarshipEntityRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(BambooHopperEntity.class, BambooHopperEntityRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(immersive_aircraft.entity.bullet.BulletEntity.class, immersive_aircraft.client.render.entity.renderer.bullet.BulletEntityRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(immersive_aircraft.entity.bullet.TinyTNT.class, immersive_aircraft.client.render.entity.renderer.bullet.TinyTNTRenderer::new);
    }
}
