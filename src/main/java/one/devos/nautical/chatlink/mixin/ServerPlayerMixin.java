package one.devos.nautical.chatlink.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.level.ServerPlayer;
import one.devos.nautical.chatlink.discord.WebHook;
import one.devos.nautical.chatlink.duck.PlayerWebHook;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements PlayerWebHook {
    private final WebHook teabridge$webHook = new WebHook(
        () -> ((ServerPlayer) (Object) this).getDisplayName().getString(),
        "https://api.nucleoid.xyz/skin/face/256/" + ((ServerPlayer) (Object) this).getStringUUID()
    );

    @Override
    public WebHook getWebHook() {
        return teabridge$webHook;
    }
}
