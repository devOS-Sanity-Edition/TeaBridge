package one.devos.nautical.teabridge;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().disableHtmlEscaping().create();
    public static Config INSTANCE;

    public static void load() throws Exception {
        var configPath = PlatformUtil.getConfigDir().resolve("teabridge.json");

        if (Files.exists(configPath)) {
            INSTANCE = GSON.fromJson(Files.readString(configPath), Config.class);
        } else {
            INSTANCE = new Config();
        }
        Files.writeString(configPath, GSON.toJson(INSTANCE), StandardCharsets.UTF_8);
    }

    @Expose public Discord discord = new Discord();
    @Expose public Avatars avatars = new Avatars();
    @Expose public Game game = new Game();
    @Expose public Crashes crashes = new Crashes();

    @Expose public boolean debug = false;

    public static class Discord {
        @Expose public String token = "";
        @Expose public String webhook = "";

        @Expose public int pkMessageDelay = 0;
        @Expose public boolean pkMessageDelayMilliseconds = true;
    }

    public static class Avatars {
        @Expose public String avatarUrl = "https://api.nucleoid.xyz/skin/face/256/%s";
        @Expose public boolean useTextureId = false;
    }

    public static class Game {
        @Expose public String serverStartingMessage = "Server is starting...";
        @Expose public String serverStartMessage = "Server has started!";
        @Expose public String serverStopMessage = "Server has stopped!";
        @Expose public String serverCrashMessage = "Server has crashed!";

        @Expose public boolean mirrorJoin = true;
        @Expose public boolean mirrorLeave = true;
        @Expose public boolean mirrorDeath = true;
        @Expose public boolean mirrorAdvancements = true;
        @Expose public boolean mirrorCommandMessages = true;
    }

    public static class Crashes {
        @Expose public boolean uploadToMclogs = true;
    }
}
