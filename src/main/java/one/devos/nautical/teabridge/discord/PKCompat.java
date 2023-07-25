package one.devos.nautical.teabridge.discord;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import one.devos.nautical.teabridge.Config;
import one.devos.nautical.teabridge.TeaBridge;
import one.devos.nautical.teabridge.duck.MessageEventProxyTag;


class PKCompat {
    private static final String MESSAGE_ENDPOINT = "https://api.pluralkit.me/v2/messages/{id}";

    private static final Object2ObjectOpenHashMap<MessageReceivedEvent, Consumer<MessageReceivedEvent>> AWAITING = new Object2ObjectOpenHashMap<>();

    static void await(MessageReceivedEvent event, Consumer<MessageReceivedEvent> handler) {
        if (Config.INSTANCE.discord.pkMessageDelay > 0) {
            AWAITING.put(event, handler);
        } else {
            handler.accept(event);
        }
    }

    private static void check() {
        for (MessageReceivedEvent event : AWAITING.keySet()) {
            if (proxied(event.getMessageIdLong())) ((MessageEventProxyTag) event).teabridge$setProxied();
            AWAITING.remove(event).accept(event);
        }
    }

    private static boolean proxied(long messageId) {
        try {
            var response = TeaBridge.CLIENT.send(HttpRequest.newBuilder()
                .uri(URI.create(MESSAGE_ENDPOINT.replace("{id}", Long.toString(messageId))))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());
            return response.statusCode() != 404;
        } catch (Exception e) {
            return false;
        }
    }

    static {
        if (Config.INSTANCE.discord.pkMessageDelay > 0)
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(PKCompat::check, 0, Config.INSTANCE.discord.pkMessageDelay, TimeUnit.SECONDS);
    }
}
