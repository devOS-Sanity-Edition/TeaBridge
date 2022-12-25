package one.devos.nautical.teabridge;

import java.net.http.HttpClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import one.devos.nautical.teabridge.discord.ChannelListener;
import one.devos.nautical.teabridge.discord.Discord;

public class TeaBridge implements DedicatedServerModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("TeaBridge");

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient().create();

    public static final HttpClient CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    @Override
    public void onInitializeServer() {
        try {
            Config.load();
        } catch (Exception e) {
            LOGGER.warn("Failed to load config using defaults : ", e);
        }

        CommandRegistrationCallback.EVENT.register(this::registerCommands);

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ChannelListener.INSTANCE.setServer(server);
            Discord.start();
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> Discord.send(Config.INSTANCE.game.serverStartMessage));

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            Discord.stop();
            Discord.send(Config.INSTANCE.game.serverStopMessage);
        });
    }
 
    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ctx, Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("teabridge").then(
            Commands.literal("reloadConfig").executes(command -> {
                try {
                    Config.load();
                    command.getSource().sendSuccess(Component.literal("Config reloaded!").withStyle(ChatFormatting.GREEN), false);
                    command.getSource().sendSystemMessage(Component.literal("Warning: options in the discord category will not be reloaded!").withStyle(ChatFormatting.YELLOW));
                } catch (Exception e) {
                    command.getSource().sendFailure(Component.literal("Failed to reload config!").withStyle(ChatFormatting.RED));
                    LOGGER.warn("Failed to reload config : ", e);
                }

                return Command.SINGLE_SUCCESS;
            })
        ));
    }
}
