package one.devos.nautical.teabridge.mixin;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import one.devos.nautical.teabridge.Config;
import one.devos.nautical.teabridge.discord.Discord;
import one.devos.nautical.teabridge.discord.ProtoWebHook;
import one.devos.nautical.teabridge.duck.PlayerWebHook;

import java.util.Objects;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements PlayerWebHook {
    private final ProtoWebHook teabridge$webHook = new ProtoWebHook(
        () -> Objects.requireNonNull(((ServerPlayer) (Object) this).getDisplayName()).getString(),
        () -> {
            ServerPlayer self = (ServerPlayer) (Object) this;
            if (Config.INSTANCE.avatars().useTextureId()) {
                MinecraftProfileTexture skin = self.getServer().getSessionService().getTextures(self.getGameProfile()).skin();
                if (skin != null) return Config.INSTANCE.avatars().avatarUrl().formatted(skin.getHash());
            }
            return Config.INSTANCE.avatars().avatarUrl().formatted(self.getStringUUID());
        }
    );

    @Override
    public ProtoWebHook getWebHook() {
        return teabridge$webHook;
    }

    @ModifyArg(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"), index = 0)
    private Component teabridge$mirrorDeathMessage(Component deathMessage) {
        if (Config.INSTANCE.game().mirrorDeath()) Discord.send("**" + deathMessage.getString() + "**");
        return deathMessage;
    }
}
