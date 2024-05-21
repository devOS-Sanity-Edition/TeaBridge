package one.devos.nautical.teabridge;

import java.net.http.HttpClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import one.devos.nautical.teabridge.discord.ChannelListener;
import one.devos.nautical.teabridge.discord.Discord;
import one.devos.nautical.teabridge.duck.PlayerWebHook;
import one.devos.nautical.teabridge.util.CrashHandler;
import one.devos.nautical.teabridge.util.StyledChatCompat;

public class TeaBridge {
    public static final Logger LOGGER = LoggerFactory.getLogger("TeaBridge");

    public static final HttpClient CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    public static void initialize() {
        try {
            Config.load();
        } catch (Exception e) {
            LOGGER.warn("Failed to load config using defaults : ", e);
        }
        Discord.start();

        PlatformUtil.registerCommand(TeaBridge::registerCommands);
    }

    public static void onServerStarting(MinecraftServer server) {
        if (Config.INSTANCE.debug()) TeaBridge.LOGGER.warn("DEBUG MODE IS ENABLED, THIS WILL LOG EVERYTHING WILL CAUSE LAG SPIKES!!!!!!");
        Discord.send(Config.INSTANCE.game().serverStartingMessage());
    }

    public static void onServerStart(MinecraftServer server) {
        ChannelListener.INSTANCE.setServer(server);
        Discord.send(Config.INSTANCE.game().serverStartMessage());
    }

    public static void onServerStop(MinecraftServer server) {
        if (!CrashHandler.CRASH_VALUE.get()) Discord.send(Config.INSTANCE.game().serverStopMessage());
        Discord.stop();
    }

    public static void onChatMessage(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound params) {
        if (sender != null) {
            ((PlayerWebHook) sender).send(message);
        } else {
            Discord.send(StyledChatCompat.modify(message).getLeft());
        }
    }

    public static void onCommandMessage(PlayerChatMessage message, CommandSourceStack source, ChatType.Bound params) {
        if (!Config.INSTANCE.game().mirrorCommandMessages()) return;
        if (!source.isPlayer()) Discord.send(message.signedContent());
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("teabridge").then(
            Commands.literal("reloadConfig").executes(command -> {
                try {
                    Config.load();
                    command.getSource().sendSuccess(() -> Component.literal("Config reloaded!").withStyle(ChatFormatting.GREEN), false);
                } catch (Exception e) {
                    command.getSource().sendFailure(Component.literal("Failed to reload config!").withStyle(ChatFormatting.RED));
                    LOGGER.warn("Failed to reload config : ", e);
                }

                return Command.SINGLE_SUCCESS;
            })
        ));
    }
}
