package one.devos.nautical.teabridge;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.google.gson.annotations.Expose;

import net.fabricmc.loader.api.FabricLoader;

public class Config {
    public static Config INSTANCE;

    public static void load() throws Exception {
        var configPath = FabricLoader.getInstance().getConfigDir().resolve("teabridge.json");

        if (Files.exists(configPath)) {
            INSTANCE = TeaBridge.GSON.fromJson(Files.readString(configPath), Config.class);
        } else {
            INSTANCE = new Config();
        }
        Files.writeString(configPath, TeaBridge.GSON.toJson(INSTANCE), StandardCharsets.UTF_8);
    }

    @Expose public Discord discord = new Discord();
    @Expose public Game game = new Game();
    @Expose public Crashes crashes = new Crashes();

    public static class Discord {
        @Expose public String token = "";
        @Expose public String guild = "";
        @Expose public String channel = "";
        @Expose public String webhook = "";
    }

    public static class Game {
        @Expose public String serverStartingMessage = "Server is starting...";
        @Expose public String serverStartMessage = "Server has started!";
        @Expose public String serverStopMessage = "Server has stopped!";
        @Expose public String serverCrashMessage = "Server has crashed!";

        @Expose public boolean mirrorDeath = true;
        @Expose public boolean mirrorAdvancements = true;
    }

    public static class Crashes {
        @Expose public boolean uploadToMclogs = true;
    }
}
