package one.devos.nautical.teabridge;

import java.net.http.HttpClient;
import java.nio.file.Path;

import com.mojang.serialization.DataResult;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.resources.ResourceLocation;
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

public class TeaBridge implements DedicatedServerModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("TeaBridge");

    public static final HttpClient CLIENT = HttpClient.newBuilder().build();

    public static final String MOD_ID = "teabridge";
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");

    public static Config config = Config.DEFAULT;

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStop);

        ResourceLocation phaseId = ResourceLocation.fromNamespaceAndPath(MOD_ID, "mirror");
        ServerMessageEvents.CHAT_MESSAGE.addPhaseOrdering(ResourceLocation.fromNamespaceAndPath("switchy_proxy", "set_args"), phaseId);
        ServerMessageEvents.CHAT_MESSAGE.addPhaseOrdering(phaseId, ResourceLocation.fromNamespaceAndPath("switchy_proxy", "clear"));
        ServerMessageEvents.CHAT_MESSAGE.register(phaseId, this::onChatMessage);

        ServerMessageEvents.COMMAND_MESSAGE.register(this::onCommandMessage);

        CommandRegistrationCallback.EVENT.register(this::registerCommands);

        Config.loadOrCreate(CONFIG_PATH)
                .ifError(e -> LOGGER.error("Failed to load config using defaults : {}", e))
                .ifSuccess(loaded -> {
                    config = loaded;
                    this.onConfigLoad();
                });
    }

    private void onConfigLoad() {
        Discord.onConfigLoad(config.discord());
    }

    private void onServerStarting(MinecraftServer server) {
        if (TeaBridge.config.debug()) TeaBridge.LOGGER.warn("!!Debug mode enabled in config!!");
        Discord.send(TeaBridge.config.game().serverStartingMessage());
    }

    private void onServerStart(MinecraftServer server) {
        ChannelListener.INSTANCE.setServer(server);
        Discord.send(TeaBridge.config.game().serverStartMessage());
    }

    private void onServerStop(MinecraftServer server) {
        if (!CrashHandler.didCrash) Discord.send(TeaBridge.config.game().serverStopMessage());
        Discord.stop();
    }

    private void onChatMessage(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound params) {
        if (sender != null) {
            ((PlayerWebHook) sender).send(message);
        } else {
            Discord.send(StyledChatCompat.modify(message).getLeft());
        }
    }

    private void onCommandMessage(PlayerChatMessage message, CommandSourceStack source, ChatType.Bound params) {
        if (!TeaBridge.config.game().mirrorCommandMessages()) return;
        if (!source.isPlayer()) Discord.send(message.signedContent());
    }

    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("teabridge")
                .requires(source -> source.hasPermission(2))
                .then(
                        Commands.literal("reloadConfig")
                                .executes(command -> {
                                    CommandSourceStack source = command.getSource();
                                    DataResult<Config> loadResult = Config.loadOrCreate(CONFIG_PATH);
                                    loadResult
                                            .ifError(e -> {
                                                source.sendFailure(
                                                        Component.literal("Failed to reload config! check log for details")
                                                                .withStyle(ChatFormatting.RED)
                                                );
                                                LOGGER.warn("Failed to reload config : {}", e);
                                            })
                                            .ifSuccess(loaded -> {
                                                config = loaded;
                                                this.onConfigLoad();
                                                source.sendSuccess(
                                                        () -> Component.literal("Config reloaded!")
                                                                .withStyle(ChatFormatting.GREEN),
                                                        false
                                                );
                                            });
                                    return loadResult.isSuccess() ? Command.SINGLE_SUCCESS : 0;
                                })
                )
        );
    }
}
