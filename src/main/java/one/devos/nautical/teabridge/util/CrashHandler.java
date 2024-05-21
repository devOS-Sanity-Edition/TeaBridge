package one.devos.nautical.teabridge.util;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonParser;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.CrashReport;
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
                Discord.send(TeaBridge.config.game().serverCrashMessage());
                onCrash.run();
            }
        }
    };

    private static final URI LOG_UPLOAD_URI = URI.create("https://api.mclo.gs/1/log");

    private static final Codec<String> LOG_UPLOAD_RESPONSE_CODEC = Codec.STRING.fieldOf("url").codec();

    public static void uploadAndSend(final CrashReport crash) {
        String message;
        try {
            HttpResponse<String> response = TeaBridge.CLIENT.send(HttpRequest.newBuilder(LOG_UPLOAD_URI)
                .POST(HttpRequest.BodyPublishers.ofString("content=" + URLEncoder.encode(crash.getDetails(), StandardCharsets.UTF_8)))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) throw new Exception("Non-success status code from request " + response);
            message = LOG_UPLOAD_RESPONSE_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(response.body())).getOrThrow();
        } catch (Exception e) {
            message = "Failed to upload crash report : " + e.getMessage();
        }

        Discord.send(message);
    }

    public interface CrashValue {
        boolean get();
        void crash(Runnable onCrash);
    }
}
