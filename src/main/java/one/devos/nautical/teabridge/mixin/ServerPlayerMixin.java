package one.devos.nautical.teabridge.mixin;

import javax.annotation.Nonnull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import one.devos.nautical.teabridge.Config;
import one.devos.nautical.teabridge.discord.Discord;
import one.devos.nautical.teabridge.discord.WebHook;
import one.devos.nautical.teabridge.duck.PlayerWebHook;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements PlayerWebHook {
    private final WebHook teabridge$webHook = new WebHook(
        () -> ((ServerPlayer) (Object) this).getDisplayName().getString(),
        "https://api.nucleoid.xyz/skin/face/256/" + ((ServerPlayer) (Object) this).getStringUUID()
    );

    @Override
    @Nonnull
    public WebHook getWebHook() {
        return teabridge$webHook;
    }

    @ModifyArg(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"), index = 0)
    private Component teabridge$mirrorDeathMessage(Component deathMessage) {
        if (!Config.INSTANCE.game.mirrorDeath) return deathMessage;
        Discord.send(deathMessage.getString());
        return deathMessage;
    }
}
