package one.devos.nautical.teabridge.duck;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.PlayerChatMessage;
import one.devos.nautical.teabridge.discord.Discord;
import one.devos.nautical.teabridge.discord.ProtoWebHook;
import one.devos.nautical.teabridge.util.StyledChatCompat;

public interface PlayerWebHook {
    final List<PlayerWebHook> ONLINE = Lists.newArrayList();

    ProtoWebHook getWebHook();

    default void send(String message, Optional<String> displayName) {
        Discord.send(getWebHook(), message, displayName);
    }

    default void send(PlayerChatMessage message) {
        var modified = StyledChatCompat.modify(message);
        send(modified.getLeft(), modified.getRight());
    }
}
