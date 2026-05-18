package me.cuboid.modding.teabridge.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import me.cuboid.modding.teabridge.TeaBridge;
import me.cuboid.modding.teabridge.discord.Discord;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
	@Definition(id = "getDeathMessage", method = "Lnet/minecraft/world/damagesource/CombatTracker;getDeathMessage()Lnet/minecraft/network/chat/Component;")
	@Expression("? = ?.getDeathMessage()")
	@Inject(method = "die", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
	private void mirrorDeathMessage(CallbackInfo ci, @Local Component message) {
		if (Discord.instance() != null && TeaBridge.config.game().mirrorDeath()) {
			Discord.instance().sendSystemMessage("**" + message.getString() + "**");
		}
	}
}
