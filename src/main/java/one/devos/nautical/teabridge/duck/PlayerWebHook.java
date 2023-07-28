package one.devos.nautical.teabridge.duck;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.PlayerChatMessage;
import one.devos.nautical.teabridge.discord.Discord;
import one.devos.nautical.teabridge.discord.WebHook;
import one.devos.nautical.teabridge.util.StyledChatCompat;

public interface PlayerWebHook {
    final List<PlayerWebHook> ONLINE = Lists.newArrayList();

    WebHook getWebHook();

    default void send(String message) {
        Discord.send(getWebHook(), message);
    }

    default void send(PlayerChatMessage message) {
        var modified = StyledChatCompat.modify(message);

        StyledChatCompat.TEMP_USERNAME = modified.getRight();
        send(modified.getLeft());
        StyledChatCompat.TEMP_USERNAME = Optional.empty();
    }
}
