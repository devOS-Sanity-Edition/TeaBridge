package one.devos.nautical.teabridge.discord;

import net.minecraft.network.chat.PlayerChatMessage;
import one.devos.nautical.teabridge.util.StyledChatCompat;

@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface PlayerWebhook {
	WebhookPrototype teabridge$prototype();

	default void teabridge$send(PlayerChatMessage message) {
		if (Discord.instance() == null)
			return;

		String proxyContent = StyledChatCompat.INSTANCE.getArg(message, StyledChatCompat.PROXY_CONTENT_ARG);
		String proxyDisplayName = StyledChatCompat.INSTANCE.getArg(message, StyledChatCompat.PROXY_DISPLAY_NAME_ARG);

		WebhookPrototype prototype = this.teabridge$prototype();
		Discord.instance().sendMessage(
				proxyDisplayName != null ? prototype.withDisplayName(proxyDisplayName) : prototype,
				proxyContent != null ? proxyContent : message.signedContent()
		);
	}
}
