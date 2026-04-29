package one.devos.nautical.teabridge.neoforge;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import one.devos.nautical.teabridge.TeaBridge;

@Mod(TeaBridge.ID)
@EventBusSubscriber(modid = TeaBridge.ID)
public final class TeaBridgeNeoForge {
	@SubscribeEvent
	private static void serverStarting(ServerStartingEvent event) {
		TeaBridge.onServerStarting(event.getServer());
	}

	@SubscribeEvent
	private static void serverStart(ServerStartedEvent event) {
		TeaBridge.onServerStart(event.getServer());
	}

	@SubscribeEvent
	private static void serverStop(ServerStoppedEvent event) {
		TeaBridge.onServerStop(event.getServer());
	}

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		TeaBridge.registerCommands(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
	}
}
