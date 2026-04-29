package one.devos.nautical.teabridge.fabric;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import one.devos.nautical.teabridge.TeaBridge;

public final class TeaBridgeFabric implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		ServerLifecycleEvents.SERVER_STARTING.register(TeaBridge::onServerStarting);
		ServerLifecycleEvents.SERVER_STARTED.register(TeaBridge::onServerStart);
		ServerLifecycleEvents.SERVER_STOPPED.register(TeaBridge::onServerStop);

		ServerMessageEvents.CHAT_MESSAGE.register(TeaBridge::onChatMessage);
		ServerMessageEvents.COMMAND_MESSAGE.register(TeaBridge::onCommandMessage);

		CommandRegistrationCallback.EVENT.register(TeaBridge::registerCommands);
	}
}
