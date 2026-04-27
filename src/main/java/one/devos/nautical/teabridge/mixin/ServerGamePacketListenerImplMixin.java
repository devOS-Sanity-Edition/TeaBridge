package one.devos.nautical.teabridge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import one.devos.nautical.teabridge.TeaBridge;
import one.devos.nautical.teabridge.discord.Discord;
import one.devos.nautical.teabridge.discord.PlayerWebhook;
import one.devos.nautical.teabridge.discord.WebhookPrototype;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin implements PlayerWebhook {
	@Shadow
	public ServerPlayer player;

	@Unique
	private WebhookPrototype webhook;

	@Override
	public WebhookPrototype teabridge$prototype() {
		if (this.webhook == null) {
			this.webhook = new WebhookPrototype(PlayerWebhook.username(this.player), PlayerWebhook.avatarUrl(this.player));
		}
		return this.webhook;
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
		if (Discord.instance() != null && TeaBridge.config.game().mirrorLeave())
			Discord.instance().sendSystemMessage(leaveMessage.getString());
		return leaveMessage;
	}
}
