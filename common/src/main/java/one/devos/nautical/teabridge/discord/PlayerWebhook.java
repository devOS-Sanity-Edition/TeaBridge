package one.devos.nautical.teabridge.discord;

import java.util.Objects;
import java.util.function.Supplier;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import one.devos.nautical.teabridge.Config;
import one.devos.nautical.teabridge.TeaBridge;

@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface PlayerWebhook {
	WebhookPrototype teabridge$prototype();

	default void teabridge$send(PlayerChatMessage message) {
		if (Discord.instance() == null)
			return;

		WebhookPrototype prototype = this.teabridge$prototype();
		Discord.instance().sendMessage(prototype, message.signedContent());
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
