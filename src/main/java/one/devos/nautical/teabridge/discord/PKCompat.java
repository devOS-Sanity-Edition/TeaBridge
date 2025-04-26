package one.devos.nautical.teabridge.discord;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import one.devos.nautical.teabridge.TeaBridge;


class PKCompat {
	private static final String MESSAGE_ENDPOINT = "https://api.pluralkit.me/v2/messages/{id}";

	private static ScheduledExecutorService scheduler;

	static void initIfEnabled() {
		if (!(TeaBridge.config.discord().pkMessageDelay() > 0)) return;
		scheduler = Executors.newScheduledThreadPool(1, r -> {
			Thread thread = new Thread(r, "TeaBridge Chat Message Scheduler");
			thread.setDaemon(true);
			return thread;
		});
	}

	static void await(MessageReceivedEvent event, BiConsumer<MessageReceivedEvent, Boolean> handler) {
		if (scheduler != null) {
			scheduler.schedule(
					() -> handler.accept(event, isProxied(event.getMessageId())),
					TeaBridge.config.discord().pkMessageDelay(),
					TeaBridge.config.discord().pkMessageDelayMilliseconds() ? TimeUnit.MILLISECONDS : TimeUnit.SECONDS
			);
			return;
		}

		CompletableFuture.runAsync(() -> handler.accept(event, isProxied(event.getMessageId())));
	}

	private static boolean isProxied(String messageId) {
		try {
			var response = TeaBridge.CLIENT.send(HttpRequest.newBuilder()
					.uri(URI.create(MESSAGE_ENDPOINT.replace("{id}", messageId)))
					.GET()
					.build(), HttpResponse.BodyHandlers.ofString());
			return response.statusCode() != 404;
		} catch (Exception e) {
			return false;
		}
	}
}
