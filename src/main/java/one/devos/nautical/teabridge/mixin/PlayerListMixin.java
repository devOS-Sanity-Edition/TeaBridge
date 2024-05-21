package one.devos.nautical.teabridge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import one.devos.nautical.teabridge.Config;
import one.devos.nautical.teabridge.discord.Discord;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @ModifyArg(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"), index = 0)
    private Component teabridge$mirrorJoinMessage(Component joinMessage) {
        if (Config.INSTANCE.game().mirrorJoin()) Discord.send(joinMessage.getString());
        return joinMessage;
    }
}
