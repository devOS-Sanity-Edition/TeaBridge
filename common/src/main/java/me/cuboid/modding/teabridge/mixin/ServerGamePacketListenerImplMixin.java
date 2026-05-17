package me.cuboid.modding.teabridge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.server.players.PlayerList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import me.cuboid.modding.teabridge.TeaBridge;
import me.cuboid.modding.teabridge.discord.Discord;
import me.cuboid.modding.teabridge.discord.PlayerWebhook;
import me.cuboid.modding.teabridge.discord.WebhookPrototype;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin implements PlayerWebhook {
	@Shadow
	public ServerPlayer player;

	@Unique
	private WebhookPrototype teabridge$webhook;

	@Override
	public WebhookPrototype teabridge$prototype() {
		if (this.teabridge$webhook == null) {
			this.teabridge$webhook = new WebhookPrototype(PlayerWebhook.username(this.player), PlayerWebhook.avatarUrl(this.player));
		}
		return this.teabridge$webhook;
	}

	@WrapOperation(
			method = "removePlayerFromWorld",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
			)
	)
	private void mirrorLeaveMessage(PlayerList instance, Component message, boolean overlay, Operation<Void> original) {
		original.call(instance, message, overlay);
		if (Discord.instance() != null && TeaBridge.config.game().mirrorLeave()) {
			Discord.instance().sendSystemMessage(message.getString());
		}
	}
}
