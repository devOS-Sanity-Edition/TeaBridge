package one.devos.nautical.teabridge.discord;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import one.devos.nautical.teabridge.util.LRUHashMap;


// TODO: Rework this somehow to not delay regular messages
class ExperimentalPKCompat {
    private static final String MESSAGE_ENDPOINT = "https://api.pluralkit.me/v2/messages/{id}";

    private static final Long2ObjectOpenHashMap<Consumer<MessageReceivedEvent>> AWAITING = new Long2ObjectOpenHashMap<>();
    private static final LRUHashMap<String, PKMessage> MESSAGE_CACHE = new LRUHashMap<>(100);

    // TODO: Disable this if a release is wanted before a solution is found for the delay
    private static boolean enabled = false;

    static void awaitProxy(MessageReceivedEvent event, Consumer<MessageReceivedEvent> handler) {
        if (enabled) {
            AWAITING.put(event.getMessageIdLong(), handler);
        } else {
            handler.accept(event);
        }
    }

    // static boolean isFromBot(Message message) {
    //     try {
    //         var response = TeaBridge.CLIENT.send(HttpRequest.newBuilder()
    //             .uri(URI.create(MESSAGE_ENDPOINT.replace("{id}", message.getId())))
    //             .GET()
    //             .build(), HttpResponse.BodyHandlers.ofString());
    //         System.out.println(response.body());
    //         return response.statusCode() / 100 == 2 ? false : message.getAuthor().isBot();
    //     } catch (Exception e) {
    //         return message.getAuthor().isBot();
    //     }
    // }

    record PKMessage() { }
}
