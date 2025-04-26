package one.devos.nautical.teabridge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.network.chat.Component;
import net.minecraft.server.PlayerAdvancements;
import one.devos.nautical.teabridge.TeaBridge;
import one.devos.nautical.teabridge.discord.Discord;

@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin {
	@ModifyArg(method = "method_53637", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"), index = 0)
	private Component mirrorAwardMessage(Component awardMessage) {
		if (TeaBridge.config.game().mirrorAdvancements()) Discord.send(awardMessage.getString());
		return awardMessage;
	}
}
