package one.devos.nautical.teabridge.neoforge.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import one.devos.nautical.teabridge.TeaBridge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Inject(
			method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V",
			at = @At("HEAD")
	)
	private void onSendChatMessage(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound boundChatType, CallbackInfo ci) {
		TeaBridge.onChatMessage(message, sender, boundChatType);
	}

	@Inject(
			method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/network/chat/ChatType$Bound;)V",
			at = @At("HEAD")
	)
	private void onSendCommandMessage(PlayerChatMessage message, CommandSourceStack source, ChatType.Bound boundChatType, CallbackInfo ci) {
		TeaBridge.onCommandMessage(message, source, boundChatType);
	}
}
