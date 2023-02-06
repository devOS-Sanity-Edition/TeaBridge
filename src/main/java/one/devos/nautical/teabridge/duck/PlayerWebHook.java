package one.devos.nautical.teabridge.duck;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.PlayerChatMessage;
import one.devos.nautical.teabridge.discord.Discord;
import one.devos.nautical.teabridge.discord.WebHook;

public interface PlayerWebHook {
    final List<PlayerWebHook> ONLINE = Lists.newArrayList();

    WebHook getWebHook();

    default void send(String message) {
        Discord.send(getWebHook(), message);
    }

    default void send(PlayerChatMessage message) {
        send(message.signedContent().plain());
    }
}
