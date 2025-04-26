package one.devos.nautical.teabridge.mixin;

import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import one.devos.nautical.teabridge.TeaBridge;
import one.devos.nautical.teabridge.discord.Discord;
import one.devos.nautical.teabridge.discord.ProtoWebHook;
import one.devos.nautical.teabridge.duck.PlayerWebHook;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements PlayerWebHook {
	private ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
		super(level, blockPos, f, gameProfile);
	}

	@Unique
	private final ProtoWebHook webHook = new ProtoWebHook(
			() -> MarkdownSanitizer.escape(Optionull.mapOrElse(this.getDisplayName(), Component::getString, () -> this.getName().getString())),
			() -> {
				if (TeaBridge.config.avatars().useTextureId()) {
					MinecraftProfileTexture skin = Objects.requireNonNull(this.getServer()).getSessionService().getTextures(this.getGameProfile()).skin();
					if (skin != null) return TeaBridge.config.avatars().avatarUrl().apply(skin.getHash());
				}
				return TeaBridge.config.avatars().avatarUrl().apply(this.getStringUUID());
			}
	);

	@Override
	public ProtoWebHook proto() {
		return webHook;
	}

	@ModifyArg(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"), index = 0)
	private Component mirrorDeathMessage(Component deathMessage) {
		if (TeaBridge.config.game().mirrorDeath()) Discord.send("**" + deathMessage.getString() + "**");
		return deathMessage;
	}
}
