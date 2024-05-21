package one.devos.nautical.teabridge.discord;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import one.devos.nautical.teabridge.Config;
import one.devos.nautical.teabridge.TeaBridge;


class PKCompat {
    private static final String MESSAGE_ENDPOINT = "https://api.pluralkit.me/v2/messages/{id}";

    private static final LinkedBlockingQueue<ScheduledMessage> scheduledMessages = new LinkedBlockingQueue<>();

    static void initIfEnabled() {
        if (!(Config.INSTANCE.discord().pkMessageDelay() > 0)) return;
        var thread = new Thread(() -> {
            while (true) {
                while (scheduledMessages.peek() == null) {}
                var message = scheduledMessages.peek();
                if (Instant.now().compareTo(message.instant) >= 0) {
                    message.handler.accept(message.event, isProxied(message.event.getMessageId()));
                    scheduledMessages.remove();
                }
            }
        }, "TeaBridge Chat Message Scheduler");
        thread.setDaemon(true);
        thread.start();
    }

    static void await(MessageReceivedEvent event, BiConsumer<MessageReceivedEvent, Boolean> handler) {
        if (Config.INSTANCE.discord().pkMessageDelay() > 0) {
            scheduledMessages.add(new ScheduledMessage(
                event,
                handler,
                Config.INSTANCE.discord().pkMessageDelayMilliseconds() ?
                    Instant.now().plusMillis(Config.INSTANCE.discord().pkMessageDelay()) : Instant.now().plusSeconds(Config.INSTANCE.discord().pkMessageDelay())
            ));
            return;
        }
        handler.accept(event, isProxied(event.getMessageId()));
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

    private record ScheduledMessage(MessageReceivedEvent event, BiConsumer<MessageReceivedEvent, Boolean> handler, Instant instant) { }
}
