package one.devos.nautical.teabridge.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.network.chat.MutableComponent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.server.players.PlayerList;
import one.devos.nautical.teabridge.TeaBridge;
import one.devos.nautical.teabridge.discord.Discord;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Inject(
			method = "placeNewPlayer",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
			)
	)
	private void mirrorJoinMessage(CallbackInfo ci, @Local MutableComponent message) {
		if (Discord.instance() != null && TeaBridge.config.game().mirrorJoin()) {
			Discord.instance().sendSystemMessage(message.getString());
		}
	}
}
