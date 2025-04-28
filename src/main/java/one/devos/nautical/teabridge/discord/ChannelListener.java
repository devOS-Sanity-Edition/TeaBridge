package one.devos.nautical.teabridge.discord;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import one.devos.nautical.teabridge.TeaBridge;
import one.devos.nautical.teabridge.util.FormattingUtils;

public class ChannelListener extends ListenerAdapter {
	public static final ChannelListener INSTANCE = new ChannelListener();

	private MinecraftServer server;
	private long channel;

	public void setServer(MinecraftServer server) {
		this.server = server;
	}

	void setChannel(long channel) {
		this.channel = channel;
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent receivedEvent) {
		PKCompat.await(receivedEvent, (event, proxied) -> {
			if (this.server == null) return;

			if (
					!event.isFromGuild() ||
							event.getChannel().getIdLong() != this.channel ||
							event.getAuthor().getIdLong() == Discord.selfMember().getUser().getIdLong() ||
							(event.isWebhookMessage() && !proxied) ||
							(!event.isWebhookMessage() && proxied)
			) return;

			PlayerList playerList = this.server.getPlayerList();

			Optional<MutableComponent> formattedMessage;
			try {
				formattedMessage = FormattingUtils.formatMessage(event.getMessage());
			} catch (Exception e) {
				TeaBridge.LOGGER.error("Exception when handling message : ", e);
				formattedMessage = Optional.of(Component.literal("Exception when handling message, check log for details!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
			}
			formattedMessage.ifPresent(mutableComponent -> playerList.broadcastSystemMessage(mutableComponent, false));
		});
	}
}
