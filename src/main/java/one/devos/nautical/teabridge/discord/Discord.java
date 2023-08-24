package one.devos.nautical.teabridge.discord;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.GatewayIntent;
import one.devos.nautical.teabridge.TeaBridge;
import one.devos.nautical.teabridge.util.JsonUtils;
import one.devos.nautical.teabridge.Config;

public class Discord {
    private static JDA jda;
    private static long guild;
    public static long selfId;

    private static Member cachedSelfMember;
    private static Supplier<Member> cachingSelfMemberGet = () -> {
        if (cachedSelfMember == null) cachedSelfMember = jda.getGuildById(guild).getSelfMember();
        return cachedSelfMember;
    };

    public static final ProtoWebHook WEB_HOOK = new ProtoWebHook(
        () -> cachingSelfMemberGet.get().getEffectiveName(),
        () -> cachingSelfMemberGet.get().getEffectiveAvatarUrl()
    );

    private static Thread messageThread;
    private static final LinkedBlockingQueue<ScheduledMessage> scheduledMessages = new LinkedBlockingQueue<>();

    public static void start() {
        if (Config.INSTANCE.discord.token.isEmpty()) {
            TeaBridge.LOGGER.error("Unable to load, no Discord token is specified!");
            return;
        }

        if (Config.INSTANCE.discord.webhook.isEmpty()) {
            TeaBridge.LOGGER.error("Unable to load, no Discord webhook is specified!");
            return;
        }

        try {
            // Get required data from webhook
            var response = TeaBridge.CLIENT.send(HttpRequest.newBuilder()
                .uri(URI.create(Config.INSTANCE.discord.webhook))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) throw new Exception("Non-success status code from request " + response);
            var webHookDataResponse = JsonUtils.GSON.fromJson(response.body(), WebHookDataResponse.class);
            if (Config.INSTANCE.debug) TeaBridge.LOGGER.warn("Webhook response : " + response.body());
            guild = Long.parseLong(webHookDataResponse.guildId);
            ChannelListener.INSTANCE.setChannel(Long.parseLong(webHookDataResponse.channelId));

            jda = JDABuilder.createDefault(Config.INSTANCE.discord.token)
                .enableIntents(List.of(GatewayIntent.MESSAGE_CONTENT))
                .addEventListeners(ChannelListener.INSTANCE, CommandUtils.INSTANCE)
                .build();

            selfId = jda.getSelfUser().getIdLong();
        } catch (Exception e) {
            TeaBridge.LOGGER.error("Exception initializing JDA", e);
            return;
        }

        messageThread = new Thread(() -> {
            while (true) {
                try {
                    Discord.scheduledSend(scheduledMessages.take());
                } catch (InterruptedException e) { }
            }
        }, "TeaBridge Discord Message Scheduler");
        messageThread.setDaemon(true);
        messageThread.start();

        PKCompat.initIfEnabled();
    }

    public static void send(String message) {
        send(WEB_HOOK, message, Optional.empty());
    }

    public static void send(ProtoWebHook webHook, String message, Optional<String> displayName) {
        scheduledMessages.add(new ScheduledMessage(webHook, message, displayName));
    }

    public static boolean scheduledSend(ScheduledMessage scheduledMessage) {
        var webHook = scheduledMessage.webHook;
        var message = scheduledMessage.message;
        var displayName = scheduledMessage.displayName;
        if (jda != null) {
            try {
                if (Config.INSTANCE.debug) TeaBridge.LOGGER.warn("Sent webhook message json : " + webHook.jsonWithContent(message, displayName));
                var response = TeaBridge.CLIENT.send(HttpRequest.newBuilder()
                    .uri(URI.create(Config.INSTANCE.discord.webhook))
                    .POST(HttpRequest.BodyPublishers.ofString(webHook.jsonWithContent(message, displayName)))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .build(), HttpResponse.BodyHandlers.ofString());
                if (Config.INSTANCE.debug) TeaBridge.LOGGER.warn("Webhook message response : " + response.body());
                if (response.statusCode() / 100 != 2) throw new Exception("Non-success status code from request " + response);
            } catch (Exception e) {
                TeaBridge.LOGGER.warn("Failed to send webhook message to discord : ", e);
            }
        }
        return true;
    }

    public static void stop() {
        if (jda != null) {
            jda.shutdown();
            jda = null;
        }
    }

    private static record WebHookDataResponse(@Expose @SerializedName("guild_id") String guildId, @Expose @SerializedName("channel_id") String channelId) { }

    public static record ScheduledMessage(ProtoWebHook webHook, String message, Optional<String> displayName) { }
}
