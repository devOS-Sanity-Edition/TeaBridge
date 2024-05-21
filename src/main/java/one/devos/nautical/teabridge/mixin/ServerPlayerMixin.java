package one.devos.nautical.teabridge.mixin;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import one.devos.nautical.teabridge.TeaBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import one.devos.nautical.teabridge.discord.Discord;
import one.devos.nautical.teabridge.discord.ProtoWebHook;
import one.devos.nautical.teabridge.duck.PlayerWebHook;

import java.util.Objects;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements PlayerWebHook {
    @Unique
    private final ProtoWebHook webHook = new ProtoWebHook(
        () -> Objects.requireNonNull(((ServerPlayer) (Object) this).getDisplayName()).getString(),
        () -> {
            ServerPlayer self = (ServerPlayer) (Object) this;
            if (TeaBridge.config.avatars().useTextureId()) {
                MinecraftProfileTexture skin = Objects.requireNonNull(self.getServer()).getSessionService().getTextures(self.getGameProfile()).skin();
                if (skin != null) return TeaBridge.config.avatars().avatarUrl().apply(skin.getHash());
            }
            return TeaBridge.config.avatars().avatarUrl().apply(self.getStringUUID());
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
