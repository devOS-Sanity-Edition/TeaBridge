package one.devos.nautical.teabridge;

import java.net.http.HttpClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.DataResult;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import one.devos.nautical.teabridge.discord.ChannelListener;
import one.devos.nautical.teabridge.discord.Discord;
import one.devos.nautical.teabridge.discord.PlayerWebHook;
import one.devos.nautical.teabridge.util.CrashHandler;

public class TeaBridge implements DedicatedServerModInitializer {
	public static final String ID = "teabridge";
	public static final Logger LOGGER = LoggerFactory.getLogger("TeaBridge");

	public static final HttpClient CLIENT = HttpClient.newBuilder().build();

	public static Config config = Config.DEFAULT;

	@Override
	public void onInitializeServer() {
		ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStart);
		ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStop);

		ResourceLocation phaseId = ResourceLocation.fromNamespaceAndPath(ID, "mirror");
		ServerMessageEvents.CHAT_MESSAGE.addPhaseOrdering(ResourceLocation.fromNamespaceAndPath("switchy_proxy", "set_args"), phaseId);
		ServerMessageEvents.CHAT_MESSAGE.addPhaseOrdering(phaseId, ResourceLocation.fromNamespaceAndPath("switchy_proxy", "clear"));
		ServerMessageEvents.CHAT_MESSAGE.register(phaseId, this::onChatMessage);

		ServerMessageEvents.COMMAND_MESSAGE.register(this::onCommandMessage);

		CommandRegistrationCallback.EVENT.register(this::registerCommands);

		Config.load()
				.ifError(e -> LOGGER.error("Failed to load config using defaults : {}", e))
				.ifSuccess(loaded -> {
					config = loaded;
					if (config.debug())
						LOGGER.warn("!!Debug mode enabled in config!!");
					this.onConfigLoad();
				});
	}

	private void onConfigLoad() {
		Discord.onConfigLoad(config.discord());
	}

	private void onServerStarting(MinecraftServer server) {
		Discord.send(config.game().serverStartingMessage());
	}

	private void onServerStart(MinecraftServer server) {
		ChannelListener.INSTANCE.setServer(server);
		Discord.send(config.game().serverStartMessage());
	}

	private void onServerStop(MinecraftServer server) {
		if (!CrashHandler.didCrash) Discord.send(config.game().serverStopMessage());
		Discord.stop();
	}

	private void onChatMessage(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound params) {
		((PlayerWebHook) sender.connection).teabridge$send(message);
	}

	private void onCommandMessage(PlayerChatMessage message, CommandSourceStack source, ChatType.Bound params) {
		if (!config.game().mirrorCommandMessages()) return;
		if (!source.isPlayer()) Discord.send(message.signedContent());
	}

	private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
		dispatcher.register(Commands.literal(ID)
				.requires(source -> source.hasPermission(2))
				.then(
						Commands.literal("reloadConfig")
								.executes(command -> {
									CommandSourceStack source = command.getSource();
									DataResult<Config> loadResult = Config.load();
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
