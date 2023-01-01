package one.devos.nautical.teabridge.discord;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import one.devos.nautical.teabridge.TeaBridge;
import one.devos.nautical.teabridge.Config;

public class Discord {
    // We can't use the Gson instance from the TeaBridge class since it has html escaping disabled, which we want enabled for obvious reasons
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();

    private static JDA jda;

    public static void start() {
        if (Config.INSTANCE.discord.token.isEmpty()) {
            TeaBridge.LOGGER.error("Unable to load, no Discord token is specified!");
            return;
        }

        if (Config.INSTANCE.discord.channel.isEmpty()) {
            TeaBridge.LOGGER.error("Unable to load, no Discord channel is specified!");
            return;
        }

        if (Config.INSTANCE.discord.webhook.isEmpty()) {
            TeaBridge.LOGGER.error("Unable to load, no Discord webhook is specified!");
            return;
        }

        try {
            jda = JDABuilder.createDefault(Config.INSTANCE.discord.token)
                .enableIntents(List.of(GatewayIntent.MESSAGE_CONTENT))
                .addEventListeners(ChannelListener.INSTANCE)
                .build();
        } catch (Exception e) {
            TeaBridge.LOGGER.error("Exception initializing JDA", e);
        }
    }

    // use a disguised webhook to avoid delay and rate-limiting
    public static void send(String message) {
        try {
            WebHook.CONVERTING_MAP.put("content", message);
            WebHook.CONVERTING_MAP.put("allowed_mentions", WebHook.AllowedMentions.INSTANCE);

            var selfMember = jda.getGuildById(Config.INSTANCE.discord.guild).getSelfMember();
            WebHook.CONVERTING_MAP.put("username", selfMember.getEffectiveName());
            WebHook.CONVERTING_MAP.put("avatar_url", selfMember.getEffectiveAvatarUrl());

            var response = TeaBridge.CLIENT.send(HttpRequest.newBuilder()
                .uri(URI.create(Config.INSTANCE.discord.webhook))
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(WebHook.CONVERTING_MAP)))
                .header("Content-Type", "application/json; charset=utf-8")
                .build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) throw new Exception("Non-success status code from request " + response);
        } catch (Exception e) {
            TeaBridge.LOGGER.warn("Failed to send webhook message to discord : ", e);
        }
    }

    public static void send(WebHook webHook, String message) {
        try {
            WebHook.CONVERTING_MAP.put("content", message);
            WebHook.CONVERTING_MAP.put("allowed_mentions", WebHook.AllowedMentions.INSTANCE);
            WebHook.CONVERTING_MAP.put("username", webHook.username().get());
            WebHook.CONVERTING_MAP.put("avatar_url", webHook.avatar());

            var response = TeaBridge.CLIENT.send(HttpRequest.newBuilder()
                .uri(URI.create(Config.INSTANCE.discord.webhook))
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(WebHook.CONVERTING_MAP)))
                .header("Content-Type", "application/json; charset=utf-8")
                .build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) throw new Exception("Non-success status code from request " + response);
        } catch (Exception e) {
            TeaBridge.LOGGER.warn("Failed to send webhook message to discord : ", e);
        }
    }

    public static void stop() {
        if (jda != null) {
            jda.shutdown();
            jda = null;
        }
    }
}
