package one.devos.nautical.teabridge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import one.devos.nautical.teabridge.TeaBridge;
import one.devos.nautical.teabridge.discord.Discord;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
	@ModifyArg(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"), index = 0)
	private Component mirrorDeathMessage(Component deathMessage) {
		if (TeaBridge.config.game().mirrorDeath())
			Discord.send("**" + deathMessage.getString() + "**");
		return deathMessage;
	}
}
