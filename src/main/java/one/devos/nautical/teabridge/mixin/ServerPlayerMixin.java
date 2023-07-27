package one.devos.nautical.teabridge.mixin;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
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
        () -> {
            ServerPlayer self = (ServerPlayer) (Object) this;
            if (Config.INSTANCE.avatars.useTextureId) {
                MinecraftProfileTexture skin = self.getServer().getSessionService().getTextures(self.getGameProfile(), true).get(MinecraftProfileTexture.Type.SKIN);
                if (skin != null) return Config.INSTANCE.avatars.avatarUrl + skin.getHash();
            }
            return Config.INSTANCE.avatars.avatarUrl + self.getStringUUID();
        }
    );

    @Override
    public WebHook getWebHook() {
        return teabridge$webHook;
    }

    @ModifyArg(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"), index = 0)
    private Component teabridge$mirrorDeathMessage(Component deathMessage) {
        if (Config.INSTANCE.game.mirrorDeath) Discord.send("**" + deathMessage.getString() + "**");
        return deathMessage;
    }
}
