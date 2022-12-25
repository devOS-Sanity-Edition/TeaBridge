package one.devos.nautical.teabridge.util;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import net.minecraft.CrashReport;
import one.devos.nautical.teabridge.Config;
import one.devos.nautical.teabridge.TeaBridge;
import one.devos.nautical.teabridge.discord.Discord;

public class CrashUploader {
    public static void upload(final CrashReport crash) {
        try {
            var response = TeaBridge.CLIENT.send(HttpRequest.newBuilder()
                .uri(URI.create("https://api.mclo.gs/1/log"))
                .POST(HttpRequest.BodyPublishers.ofString("content=" + URLEncoder.encode(crash.getDetails(), StandardCharsets.UTF_8.toString())))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) return;
            Discord.send(Config.INSTANCE.game.serverCrashMessage + "\n" + TeaBridge.GSON.fromJson(response.body(), Map.class).get("url"));
        } catch (Exception e) { }
    }
}
