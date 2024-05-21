package one.devos.nautical.teabridge.util;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.google.gson.annotations.Expose;

import net.minecraft.CrashReport;
import one.devos.nautical.teabridge.Config;
import one.devos.nautical.teabridge.TeaBridge;
import one.devos.nautical.teabridge.discord.Discord;

public class CrashHandler {
    public static final CrashValue CRASH_VALUE = new CrashValue() {
        private boolean value = false;

        @Override
        public boolean get() {
            return value;
        }

        @Override
        public void crash(Runnable onCrash) {
            if (!value) {
                value = true;
                Discord.send(Config.INSTANCE.game().serverCrashMessage());
                onCrash.run();
            }
        }
    };

    public static void uploadAndSend(final CrashReport crash) {
        String crashMessage = null;

        try {
            var response = TeaBridge.CLIENT.send(HttpRequest.newBuilder()
                .uri(URI.create("https://api.mclo.gs/1/log"))
                .POST(HttpRequest.BodyPublishers.ofString("content=" + URLEncoder.encode(crash.getDetails(), StandardCharsets.UTF_8.toString())))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) throw new Exception("Non-success status code from request " + response);
            crashMessage = JsonUtils.GSON.fromJson(response.body(), LogUploadResponse.class).url;
        } catch (Exception e) {
            crashMessage = "Failed to upload crash report : " + e.getMessage();
        }

        if (crashMessage != null) Discord.send(crashMessage);
    }

    private record LogUploadResponse(@Expose String url) { }

    public interface CrashValue {
        boolean get();
        void crash(Runnable onCrash);
    }
}
