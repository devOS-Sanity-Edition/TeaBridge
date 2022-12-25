package one.devos.nautical.teabridge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.network.chat.Component;
import net.minecraft.server.PlayerAdvancements;
import one.devos.nautical.teabridge.Config;
import one.devos.nautical.teabridge.discord.Discord;

@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin {
    @ModifyArg(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"), index = 0)
    private Component teabridge$mirrorAwardMessage(Component awardMessage) {
        if (Config.INSTANCE.game.mirrorAdvancements) Discord.send(awardMessage.getString());
        return awardMessage;
    }
}
