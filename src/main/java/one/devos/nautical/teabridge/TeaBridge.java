package one.devos.nautical.teabridge;

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
import one.devos.nautical.teabridge.discord.Discord;
import one.devos.nautical.teabridge.discord.PlayerWebhook;
import one.devos.nautical.teabridge.util.CrashHandler;

public class TeaBridge implements DedicatedServerModInitializer {
	public static final String ID = "teabridge";
	public static final Logger LOGGER = LoggerFactory.getLogger("TeaBridge");

	public static Config config;

	private static Discord discord;

	@Override
	public void onInitializeServer() {
		ServerLifecycleEvents.SERVER_STARTING.register(TeaBridge::onServerStarting);
		ServerLifecycleEvents.SERVER_STARTED.register(TeaBridge::onServerStart);
		ServerLifecycleEvents.SERVER_STOPPED.register(TeaBridge::onServerStop);

		ResourceLocation phaseId = ResourceLocation.fromNamespaceAndPath(ID, "mirror");
		ServerMessageEvents.CHAT_MESSAGE.addPhaseOrdering(ResourceLocation.fromNamespaceAndPath("switchy_proxy", "set_args"), phaseId);
		ServerMessageEvents.CHAT_MESSAGE.addPhaseOrdering(phaseId, ResourceLocation.fromNamespaceAndPath("switchy_proxy", "clear"));
		ServerMessageEvents.CHAT_MESSAGE.register(phaseId, TeaBridge::onChatMessage);

		ServerMessageEvents.COMMAND_MESSAGE.register(TeaBridge::onCommandMessage);

		CommandRegistrationCallback.EVENT.register(TeaBridge::registerCommands);
	}

	private static void onConfigLoad(Config config, MinecraftServer server) {
		TeaBridge.config = config;

		if (TeaBridge.discord != null)
			TeaBridge.discord.shutdown();
		TeaBridge.discord = Discord.initialize(config.discord(), server);
	}

	private static void onServerStarting(MinecraftServer server) {
		Config.load()
				.ifError(e -> LOGGER.error("Failed to load config using defaults : {}", e))
				.ifSuccess(config -> onConfigLoad(config, server));

		if (Discord.instance() != null)
			Discord.instance().sendSystemMessage(config.game().serverStartingMessage());
	}

	private static void onServerStart(MinecraftServer server) {
		if (Discord.instance() != null)
			Discord.instance().sendSystemMessage(config.game().serverStartMessage());
	}

	private static void onServerStop(MinecraftServer server) {
		Discord discord = Discord.instance();
		if (discord == null)
			return;

		if (!CrashHandler.didCrash)
			discord.sendSystemMessage(config.game().serverStopMessage());
		discord.shutdown();
	}

	private static void onChatMessage(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound params) {
		((PlayerWebhook) sender.connection).teabridge$send(message);
	}

	private static void onCommandMessage(PlayerChatMessage message, CommandSourceStack source, ChatType.Bound params) {
		if (!config.game().mirrorCommandMessages())
			return;

		if (Discord.instance() != null && !source.isPlayer())
			Discord.instance().sendSystemMessage(message.signedContent());
	}

	private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
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
											.ifSuccess(config -> {
												onConfigLoad(config, source.getServer());
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
