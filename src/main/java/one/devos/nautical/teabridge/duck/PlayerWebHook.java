package one.devos.nautical.teabridge.duck;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.PlayerChatMessage;
import one.devos.nautical.teabridge.discord.Discord;
import one.devos.nautical.teabridge.discord.ProtoWebHook;
import one.devos.nautical.teabridge.util.StyledChatCompat;

public interface PlayerWebHook {
	List<PlayerWebHook> ONLINE = Lists.newArrayList();

	ProtoWebHook proto();

	default void send(String message, @Nullable String displayName) {
		Discord.send(proto(), message, displayName);
	}

	default void send(PlayerChatMessage message) {
		var modified = StyledChatCompat.modify(message);
		send(modified.getLeft(), modified.getRight());
	}
}
