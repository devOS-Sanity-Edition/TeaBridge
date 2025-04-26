package one.devos.nautical.teabridge.discord;

import net.minecraft.network.chat.PlayerChatMessage;
import one.devos.nautical.teabridge.util.StyledChatCompat;

@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface PlayerWebHook {
	WebHookPrototype teabridge$prototype();

	default void teabridge$send(PlayerChatMessage message) {
		String proxyContent = StyledChatCompat.INSTANCE.getArg(message, StyledChatCompat.PROXY_CONTENT_ARG);
		String proxyDisplayName = StyledChatCompat.INSTANCE.getArg(message, StyledChatCompat.PROXY_DISPLAY_NAME_ARG);
		Discord.send(this.teabridge$prototype().createMessage(proxyContent != null ? proxyContent : message.signedContent(), proxyDisplayName));
	}
}
