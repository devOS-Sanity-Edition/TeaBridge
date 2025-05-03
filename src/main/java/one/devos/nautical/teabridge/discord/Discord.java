package one.devos.nautical.teabridge.discord;

import java.util.Collections;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.IncomingWebhookClient;
import net.dv8tion.jda.api.entities.WebhookClient;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.Route;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.dv8tion.jda.internal.requests.RestActionImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import one.devos.nautical.teabridge.Config;
import one.devos.nautical.teabridge.TeaBridge;
import one.devos.nautical.teabridge.util.FormattingUtils;

public class Discord {
	@Nullable
	private static Discord instance;

	private final JDA jda;
	private final IncomingWebhookClient webhookClient;

	public Discord(Config.Discord config, MinecraftServer server) {
		this.jda = JDABuilder.createDefault(config.token())
				.enableIntents(GatewayIntent.MESSAGE_CONTENT)
				.build();
		this.webhookClient = WebhookClient.createClient(this.jda, config.webhook());

		long channel = new RestActionImpl<Long>(
				this.jda, Route.Webhooks.GET_WEBHOOK.compile(this.webhookClient.getId()),
				(response, request) -> response.getObject().getUnsignedLong("channel_id")
		).complete();
		this.jda.addEventListener(new MessageBridge(channel, server));
	}

	public void sendSystemMessage(String content) {
		this.webhookClient.sendMessage(content)
				.setAllowedMentions(Collections.emptySet())
				.queue();
	}

	public void sendMessage(WebhookPrototype prototype, String content) {
		this.webhookClient.sendMessage(content)
				.setAllowedMentions(Collections.emptySet())
				.setUsername(MarkdownSanitizer.escape(prototype.username().get()))
				.setAvatarUrl(prototype.avatar().get())
				.queue();
	}

	public void shutdown() {
		if (instance == this)
			instance = null;
		this.jda.shutdown();
	}

	@Nullable
	public static Discord instance() {
		return instance;
	}

	@Nullable
	public static Discord initialize(Config.Discord config, MinecraftServer server) {
		try {
			if (instance != null)
				throw new IllegalStateException("Discord already initialized");
			instance = new Discord(config, server);
			return instance;
		} catch (Exception e) {
			TeaBridge.LOGGER.error("Exception initializing Discord", e);
			return null;
		}
	}

	private static final class MessageBridge extends ListenerAdapter {
		private final long channel;
		private final MinecraftServer server;

		private MessageBridge(long channel, MinecraftServer server) {
			this.server = server;
			this.channel = channel;
		}

		@Override
		public void onMessageReceived(@NotNull MessageReceivedEvent event) {
			if (event.getChannel().getIdLong() != this.channel || event.isWebhookMessage())
				return;

			Optional<MutableComponent> formattedMessage;
			try {
				formattedMessage = FormattingUtils.formatMessage(event.getMessage());
			} catch (Exception e) {
				TeaBridge.LOGGER.error("Exception when handling message : ", e);
				formattedMessage = Optional.of(Component.literal("Exception when handling message, check log for details!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
			}
			formattedMessage.ifPresent(message -> this.server.execute(() -> this.server.getPlayerList().broadcastSystemMessage(message, false)));
		}
	}
}
