package one.devos.nautical.teabridge.mixin;

import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import one.devos.nautical.teabridge.Config;
import one.devos.nautical.teabridge.TeaBridge;
import one.devos.nautical.teabridge.discord.PlayerWebhook;
import one.devos.nautical.teabridge.discord.WebhookPrototype;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin implements PlayerWebhook {
	@Shadow
	public ServerPlayer player;

	@Shadow
	protected abstract GameProfile playerProfile();

	@Unique
	private WebhookPrototype webhook;

	@Override
	public WebhookPrototype teabridge$prototype() {
		if (this.webhook == null) {
			this.webhook = new WebhookPrototype(
					() -> this.player.getDisplayName().getString(),
					() -> {
						Config.Avatars avatarConfig = TeaBridge.config.avatars();
						String avatarUrlFormat = avatarConfig.avatarUrl();

						if (avatarConfig.useTextureId()) {
							MinecraftProfileTexture skin = Objects.requireNonNull(this.player.getServer())
									.getSessionService()
									.getTextures(this.playerProfile()).skin();
							if (skin != null)
								return avatarUrlFormat.formatted(skin.getHash());
						}

						return avatarUrlFormat.formatted(this.player.getStringUUID());
					}
			);
		}
		return this.webhook;
	}
}
