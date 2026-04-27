package one.devos.nautical.teabridge.discord;

import java.util.Objects;
import java.util.function.Supplier;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import one.devos.nautical.teabridge.Config;
import one.devos.nautical.teabridge.TeaBridge;
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

	static Supplier<String> username(ServerPlayer player) {
		return () -> player.getDisplayName().getString();
	}

	static Supplier<String> avatarUrl(ServerPlayer player) {
		Config.Avatars avatarConfig = TeaBridge.config.avatars();
		String avatarUrlFormat = avatarConfig.avatarUrl();
		return avatarConfig.useTextureId() ? () -> {
				MinecraftProfileTexture skin = Objects.requireNonNull(player.level().getServer())
						.getSessionService()
						.getTextures(player.getGameProfile()).skin();
				return avatarUrlFormat.formatted(Objects.requireNonNull(skin).getHash());
			} : () -> avatarUrlFormat.formatted(player.getStringUUID());
	}
}
