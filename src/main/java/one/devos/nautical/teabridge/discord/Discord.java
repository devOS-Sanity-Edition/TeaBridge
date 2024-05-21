package one.devos.nautical.teabridge.discord;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.GatewayIntent;
import one.devos.nautical.teabridge.TeaBridge;
import one.devos.nautical.teabridge.util.MoreCodecs;
import org.jetbrains.annotations.Nullable;

public class Discord {
    static JDA jda;
    static Supplier<Member> selfMember;

    public static final ProtoWebHook WEB_HOOK = new ProtoWebHook(
        () -> selfMember.get().getEffectiveName(),
        () -> URI.create(selfMember.get().getEffectiveAvatarUrl())
    );

    private static final LinkedBlockingQueue<ScheduledMessage> scheduledMessages = new LinkedBlockingQueue<>();

    public static void start() {
        if (TeaBridge.config.discord().token().isEmpty()) {
            TeaBridge.LOGGER.error("Unable to load, no Discord token is specified!");
            return;
        }

        if (TeaBridge.config.discord().webhook().toString().isEmpty()) {
            TeaBridge.LOGGER.error("Unable to load, no Discord webhook is specified!");
            return;
        }

        try {
            // Get required data from webhook
            HttpResponse<String> response = TeaBridge.CLIENT.send(HttpRequest.newBuilder(TeaBridge.config.discord().webhook())
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) throw new Exception("Non-success status code from request " + response);
            WebHookData webHookData = WebHookData.fromJson(JsonParser.parseString(response.body())).getOrThrow();
            if (TeaBridge.config.debug()) TeaBridge.LOGGER.warn("Webhook response : " + response.body());
            ChannelListener.INSTANCE.setChannel(webHookData.channelId);

            jda = JDABuilder.createDefault(TeaBridge.config.discord().token())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(ChannelListener.INSTANCE)
                .build();

            selfMember = Suppliers.memoize(() -> {
                Guild guild = jda.getGuildById(webHookData.guildId);
                if (guild != null) {
                    return guild.getSelfMember();
                } else {
                    throw new RuntimeException("Guild is null. This most likely means you are missing the message content intent, please enable it within the app's settings in the discord developer portal.");
                }
            });
        } catch (Exception e) {
            TeaBridge.LOGGER.error("Exception initializing JDA", e);
            return;
        }

        Thread messageThread = new Thread(() -> {
            while (true) {
                try {
                    Discord.scheduledSend(scheduledMessages.take());
                } catch (InterruptedException ignored) { }
            }
        }, "TeaBridge Discord Message Scheduler");
        messageThread.setDaemon(true);
        messageThread.start();

        PKCompat.initIfEnabled();
    }

    public static void send(String message) {
        send(WEB_HOOK, message, null);
    }

    public static void send(ProtoWebHook webHook, String message, @Nullable String displayName) {
        scheduledMessages.add(new ScheduledMessage(webHook, message, displayName));
    }

    private static void scheduledSend(ScheduledMessage scheduledMessage) {
        ProtoWebHook webHook = scheduledMessage.webHook;
        String message = scheduledMessage.message;
        String displayName = scheduledMessage.displayName;
        if (jda != null) {
            try {
                HttpResponse<String> response = TeaBridge.CLIENT.send(HttpRequest.newBuilder(TeaBridge.config.discord().webhook())
                    .POST(HttpRequest.BodyPublishers.ofString(webHook.createMessage(message, displayName).toJson().getOrThrow()))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .build(), HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() / 100 != 2) throw new Exception("Non-success status code from request " + response);
            } catch (Exception e) {
                TeaBridge.LOGGER.warn("Failed to send webhook message to discord : ", e);
            }
        }
    }

    public static void stop() {
        if (jda != null) {
            jda.shutdown();
            jda = null;
        }
    }

    private record WebHookData(long guildId, long channelId) {
        public static final Codec<WebHookData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MoreCodecs.fromString(Long::parseUnsignedLong).fieldOf("guild_id").forGetter(WebHookData::guildId),
                MoreCodecs.fromString(Long::parseUnsignedLong).fieldOf("channel_id").forGetter(WebHookData::channelId)
        ).apply(instance, WebHookData::new));

        public static DataResult<WebHookData> fromJson(JsonElement json) {
            return CODEC.parse(JsonOps.INSTANCE, json);
        }
    }

    private record ScheduledMessage(ProtoWebHook webHook, String message, @Nullable String displayName) { }
}
