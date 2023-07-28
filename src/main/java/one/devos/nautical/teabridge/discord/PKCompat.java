package one.devos.nautical.teabridge.discord;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import one.devos.nautical.teabridge.Config;
import one.devos.nautical.teabridge.TeaBridge;


class PKCompat {
    private static final String MESSAGE_ENDPOINT = "https://api.pluralkit.me/v2/messages/{id}";

    private static final ConcurrentLinkedQueue<ScheduledEvent> scheduled = new ConcurrentLinkedQueue<>();

    static void initIfEnabled(BooleanSupplier running) {
        if (Config.INSTANCE.discord.pkMessageDelay > 0)
        new Thread(() -> {
            while (running.getAsBoolean()) {
                scheduled.removeIf(scheduledEvent -> {
                    if (Instant.now().compareTo(scheduledEvent.instant) >= 0) {
                        scheduledEvent.handler.accept(scheduledEvent.event, isProxied(scheduledEvent.event.getMessageId()));
                        return true;
                    }
                    return false;
                });
            }
        }).start();
    }

    static void await(MessageReceivedEvent event, BiConsumer<MessageReceivedEvent, Boolean> handler) {
        if (Config.INSTANCE.discord.pkMessageDelay > 0) {
            scheduled.add(new ScheduledEvent(
                event,
                handler,
                Config.INSTANCE.discord.pkMessageDelayMilliseconds ?
                    Instant.now().plusMillis(Config.INSTANCE.discord.pkMessageDelay) : Instant.now().plusSeconds(Config.INSTANCE.discord.pkMessageDelay)
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

    private record ScheduledEvent(MessageReceivedEvent event, BiConsumer<MessageReceivedEvent, Boolean> handler, Instant instant) { }
}
