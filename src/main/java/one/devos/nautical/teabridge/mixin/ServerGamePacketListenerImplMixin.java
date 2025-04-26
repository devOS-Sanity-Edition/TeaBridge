package one.devos.nautical.teabridge.mixin;

import java.net.URI;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import one.devos.nautical.teabridge.Config;
import one.devos.nautical.teabridge.TeaBridge;
import one.devos.nautical.teabridge.discord.Discord;
import one.devos.nautical.teabridge.discord.PlayerWebHook;
import one.devos.nautical.teabridge.discord.WebHookPrototype;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin implements PlayerWebHook {
	@Unique
	private WebHookPrototype webHook;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void initWebHook(MinecraftServer server, Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		this.webHook = new WebHookPrototype(
				() -> player.getDisplayName().getString(),
				() -> {
					Config.Avatars avatarConfig = TeaBridge.config.avatars();
					Function<String, URI> avatarUrlFactory = avatarConfig.avatarUrl();

					if (avatarConfig.useTextureId()) {
						MinecraftProfileTexture skin = server.getSessionService().getTextures(player.getGameProfile()).skin();
						if (skin != null)
							return avatarUrlFactory.apply(skin.getHash());
					}

					return avatarUrlFactory.apply(player.getStringUUID());
				}
		);
	}

	@Override
	public WebHookPrototype teabridge$prototype() {
		return this.webHook;
	}

	@ModifyArg(
			method = "removePlayerFromWorld",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
			),
			index = 0
	)
	private Component mirrorLeaveMessage(Component leaveMessage) {
		if (TeaBridge.config.game().mirrorLeave())
			Discord.send(leaveMessage.getString());
		return leaveMessage;
	}
}
