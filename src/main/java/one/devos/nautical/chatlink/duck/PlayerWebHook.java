package one.devos.nautical.chatlink.duck;

import org.jetbrains.annotations.Nullable;

import one.devos.nautical.chatlink.discord.Discord;
import one.devos.nautical.chatlink.discord.WebHook;

public interface PlayerWebHook {
    @Nullable WebHook getWebHook();

    default void send(String message) {
        Discord.send(getWebHook(), message);
    }
}
