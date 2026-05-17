package me.cuboid.modding.teabridge.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.PlayerAdvancements;

import net.minecraft.server.level.ServerPlayer;

import me.cuboid.modding.teabridge.TeaBridge;
import me.cuboid.modding.teabridge.discord.Discord;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {
	@SuppressWarnings("UnresolvedMixinReference")
	@WrapOperation(
			method = {"method_53637", "lambda$award$2"},
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/advancements/AdvancementType;createAnnouncement(Lnet/minecraft/advancements/AdvancementHolder;Lnet/minecraft/server/level/ServerPlayer;)Lnet/minecraft/network/chat/MutableComponent;"
			)
	)
	private MutableComponent mirrorAdvancementMessage(AdvancementType instance, AdvancementHolder holder, ServerPlayer player, Operation<MutableComponent> original) {
		MutableComponent message = original.call(instance, holder, player);
		if (Discord.instance() != null && TeaBridge.config.game().mirrorAdvancements()) {
			Discord.instance().sendSystemMessage("**" + message.getString() + "**");
		}
		return message;
	}
}
