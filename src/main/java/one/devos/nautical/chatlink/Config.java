package one.devos.nautical.chatlink;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.annotation.Nonnull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import net.fabricmc.loader.api.FabricLoader;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient().create();

    public static Config INSTANCE;

    public static void load() throws Exception {
        var configPath = FabricLoader.getInstance().getConfigDir().resolve("styled-chat.json");

        if (Files.exists(configPath)) {
            INSTANCE = GSON.fromJson(Files.readString(configPath), Config.class);
        } else {
            INSTANCE = new Config();
            Files.writeString(configPath, GSON.toJson(INSTANCE), StandardCharsets.UTF_8);
        }
    }

    @Expose @Nonnull public Discord discord = new Discord();
    @Expose @Nonnull public Game game = new Game();
    @Expose @Nonnull public Crashes crashes = new Crashes();

    public static class Discord {
        @Expose @Nonnull public String webhook = "";
        @Expose @Nonnull public String channel = "";
        @Expose @Nonnull public String token = "";
    }

    public static class Game {
        @Expose public boolean mirrorDeath = true;
        @Expose public boolean mirrorAdvancements = true;
        @Expose public boolean mirrorServerMessages = true;
    }

    public static class Crashes {
        @Expose public boolean uploadToHastebin = true;
    }
}
