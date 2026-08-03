package immersive_aircraft.cobalt.network;

import immersive_aircraft.Main;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * 1.12.2 port: SimpleNetworkWrapper instead of the 1.16 forge SimpleChannel.
 * Registration requires an explicit receiving {@link Side}.
 */
public class NetworkHandler {
    private static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Main.SHORT_MOD_ID);

    private static int id = 0;

    public static <T extends Message> void registerMessage(Class<T> msg, Side side) {
        INSTANCE.registerMessage(new Handler<T>(), msg, id++, side);
    }

    public static void sendToServer(Message m) {
        INSTANCE.sendToServer(m);
    }

    public static void sendToPlayer(Message m, EntityPlayerMP e) {
        INSTANCE.sendTo(m, e);
    }

    private static class Handler<T extends Message> implements IMessageHandler<T, IMessage> {
        @Override
        public IMessage onMessage(final T message, MessageContext ctx) {
            if (ctx.side.isServer()) {
                final EntityPlayerMP player = ctx.getServerHandler().player;
                player.getServerWorld().addScheduledTask(new Runnable() {
                    @Override
                    public void run() {
                        message.receive(player);
                    }
                });
            } else {
                Main.proxy.addScheduledTaskClient(new Runnable() {
                    @Override
                    public void run() {
                        EntityPlayer player = Main.proxy.getClientPlayer();
                        if (player != null) {
                            message.receive(player);
                        }
                    }
                });
            }
            return null;
        }
    }
}
