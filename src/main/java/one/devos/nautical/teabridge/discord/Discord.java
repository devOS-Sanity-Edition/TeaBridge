package one.devos.nautical.teabridge.discord;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.GatewayIntent;
import one.devos.nautical.teabridge.TeaBridge;
import one.devos.nautical.teabridge.Config;

public class Discord {
    private static JDA jda;
    public static final List<Runnable> jdaBuildTasks = Collections.synchronizedList(new ArrayList<>());

    private static Member cachedSelfMember;
    private static Supplier<Member> cachingSelfMemberGet = () -> {
        if (cachedSelfMember == null) {
            cachedSelfMember = jda.getGuildById(Config.INSTANCE.discord.guild).getSelfMember();
        }
        return cachedSelfMember;
    };
    public static final WebHook WEB_HOOK = new WebHook(
        () -> cachingSelfMemberGet.get().getEffectiveName(),
        () -> cachingSelfMemberGet.get().getEffectiveAvatarUrl()
    );

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
            synchronized (jdaBuildTasks) {
                jdaBuildTasks.forEach(Runnable::run);
                jdaBuildTasks.clear();
            }
        } catch (Exception e) {
            TeaBridge.LOGGER.error("Exception initializing JDA", e);
        }
    }

    public static void send(String message) {
        send(WEB_HOOK, message);
    }

    public static void send(WebHook webHook, String message) {
        if (jda != null) {
            try {
                var response = TeaBridge.CLIENT.send(HttpRequest.newBuilder()
                    .uri(URI.create(Config.INSTANCE.discord.webhook))
                    .POST(HttpRequest.BodyPublishers.ofString(webHook.jsonWithContent(message)))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .build(), HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() / 100 != 2) throw new Exception("Non-success status code from request " + response);
            } catch (Exception e) {
                TeaBridge.LOGGER.warn("Failed to send webhook message to discord : ", e);
            }
        } else {
            jdaBuildTasks.add(() -> send(webHook, message));
        }
    }

    public static void stop() {
        if (jda != null) {
            jda.shutdown();
            jda = null;
        }
    }
}
