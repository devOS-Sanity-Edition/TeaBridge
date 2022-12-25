package one.devos.nautical.teabridge.duck;

import net.minecraft.network.chat.PlayerChatMessage;
import one.devos.nautical.teabridge.discord.Discord;
import one.devos.nautical.teabridge.discord.WebHook;

public interface PlayerWebHook {
    WebHook getWebHook();

    default void send(String message) {
        Discord.send(getWebHook(), message);
    }

    default void send(PlayerChatMessage message) {
        send(message.signedContent().plain());
    }
}
