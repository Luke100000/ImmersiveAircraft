package immersive_airships.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;

import static net.minecraft.util.Formatting.*;

public class Command {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("immersive-airships")
                .then(register("help", Command::displayHelp))
                .requires((serverCommandSource) -> serverCommandSource.hasPermissionLevel(2))
        );
    }

    private static int displayHelp(CommandContext<ServerCommandSource> ctx) {
        Entity player = ctx.getSource().getEntity();
        if (player == null) {
            return 0;
        }

        sendMessage(player, DARK_RED + "--- " + GOLD + "COMMANDS" + DARK_RED + " ---");
        sendMessage(player, WHITE + " /immersive-airships help " + GOLD + " - Displays this help.");

        return 0;
    }

    private static ArgumentBuilder<ServerCommandSource, ?> register(String name, com.mojang.brigadier.Command<ServerCommandSource> cmd) {
        return CommandManager.literal(name).requires(cs -> cs.hasPermissionLevel(0)).executes(cmd);
    }

    private static ArgumentBuilder<ServerCommandSource, ?> register(String name) {
        return CommandManager.literal(name).requires(cs -> cs.hasPermissionLevel(0));
    }

    private static void success(String message, CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(new LiteralText(message).formatted(GREEN), true);
    }

    private static void fail(String message, CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendError(new LiteralText(message).formatted(RED));
    }


    private static void sendMessage(Entity commandSender, String message) {
        commandSender.sendSystemMessage(new LiteralText(message), Util.NIL_UUID);
    }
}
