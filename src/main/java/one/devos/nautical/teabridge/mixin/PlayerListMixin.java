package one.devos.nautical.teabridge.mixin;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import one.devos.nautical.teabridge.discord.Discord;
import one.devos.nautical.teabridge.duck.PlayerWebHook;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(
        method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V",
        at = @At("RETURN")
    )
    private void teabridge$mirrorChatMessages(
        PlayerChatMessage chatMessage,
        Predicate<ServerPlayer> sendToPredicate,
        ServerPlayer player,
        ChatType.Bound bound, CallbackInfo ci
    ) {
        if (player != null) {
            ((PlayerWebHook) player).send(chatMessage);
        } else {
            Discord.send(chatMessage.signedContent());
        }
    }

    @ModifyArg(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"), index = 0)
    private Component teabridge$mirrorJoinMessage(Component joinMessage) {
        Discord.send(joinMessage.getString());
        return joinMessage;
    }
}
