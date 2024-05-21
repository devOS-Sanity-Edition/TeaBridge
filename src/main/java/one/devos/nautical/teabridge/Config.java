package one.devos.nautical.teabridge;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Config(
        Discord discord,
        Avatars avatars,
        Game game,
        Crashes crashes,
        boolean debug
) {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().disableHtmlEscaping().create();

    public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Discord.CODEC.optionalFieldOf("discord", Discord.DEFAULT).forGetter(Config::discord),
            Avatars.CODEC.optionalFieldOf("avatars", Avatars.DEFAULT).forGetter(Config::avatars),
            Game.CODEC.optionalFieldOf("game", Game.DEFAULT).forGetter(Config::game),
            Crashes.CODEC.optionalFieldOf("crashes", Crashes.DEFAULT).forGetter(Config::crashes),
            Codec.BOOL.optionalFieldOf("debug", false).forGetter(Config::debug)
    ).apply(instance, Config::new));

    public static Config INSTANCE = new Config(Discord.DEFAULT, Avatars.DEFAULT, Game.DEFAULT, Crashes.DEFAULT, false);

    public static void load() throws Exception {
        Path configPath = PlatformUtil.getConfigDir().resolve("teabridge.json");
        if (Files.exists(configPath))
            INSTANCE = CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(Files.newBufferedReader(configPath))).getOrThrow();
        Files.writeString(configPath, GSON.toJson(CODEC.encodeStart(JsonOps.INSTANCE, INSTANCE).getOrThrow()), StandardCharsets.UTF_8);
    }

    public record Discord(String token, String webhook, int pkMessageDelay, boolean pkMessageDelayMilliseconds) {
        public static final String DEFAULT_TOKEN = "";
        public static final String DEFAULT_WEBHOOK = "";
        public static final int PK_MESSAGE_DELAY = 0;
        public static final boolean PK_MESSAGE_DELAY_MILLISECONDS = true;

        public static final Codec<Discord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("token", DEFAULT_TOKEN).forGetter(Discord::token),
                Codec.STRING.optionalFieldOf("webhook", DEFAULT_WEBHOOK).forGetter(Discord::webhook),
                Codec.INT.optionalFieldOf("pkMessageDelay", PK_MESSAGE_DELAY).forGetter(Discord::pkMessageDelay),
                Codec.BOOL.optionalFieldOf("pkMessageDelayMilliseconds", PK_MESSAGE_DELAY_MILLISECONDS).forGetter(Discord::pkMessageDelayMilliseconds)
        ).apply(instance, Discord::new));

        public static final Discord DEFAULT = new Discord(DEFAULT_TOKEN, DEFAULT_WEBHOOK, PK_MESSAGE_DELAY, PK_MESSAGE_DELAY_MILLISECONDS);
    }

    public record Avatars(String avatarUrl, boolean useTextureId) {
        public static final String DEFAULT_AVATAR_URL = "https://api.nucleoid.xyz/skin/face/256/%s";
        public static final boolean DEFAULT_USE_TEXTURE_ID = false;

        public static final Codec<Avatars> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("avatarUrl", DEFAULT_AVATAR_URL).forGetter(Avatars::avatarUrl),
                Codec.BOOL.optionalFieldOf("useTextureId", DEFAULT_USE_TEXTURE_ID).forGetter(Avatars::useTextureId)
        ).apply(instance, Avatars::new));

        public static final Avatars DEFAULT = new Avatars(DEFAULT_AVATAR_URL, DEFAULT_USE_TEXTURE_ID);
    }

    public record Game(
            String serverStartingMessage,
            String serverStartMessage,
            String serverStopMessage,
            String serverCrashMessage,
            boolean mirrorJoin,
            boolean mirrorLeave,
            boolean mirrorDeath,
            boolean mirrorAdvancements,
            boolean mirrorCommandMessages
    ) {
        public static final String DEFAULT_SERVER_STARTING_MESSAGE = "Server is starting...";
        public static final String DEFAULT_SERVER_START_MESSAGE = "Server has started!";
        public static final String DEFAULT_SERVER_STOP_MESSAGE = "Server has stopped!";
        public static final String DEFAULT_SERVER_CRASH_MESSAGE = "Server has crashed!";
        public static final boolean DEFAULT_MIRROR_JOIN = true;
        public static final boolean DEFAULT_MIRROR_LEAVE = true;
        public static final boolean DEFAULT_MIRROR_DEATH = true;
        public static final boolean DEFAULT_MIRROR_ADVANCEMENTS = true;
        public static final boolean DEFAULT_MIRROR_COMMAND_MESSAGES = true;

        public static final Codec<Game> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("serverStartingMessage", DEFAULT_SERVER_STARTING_MESSAGE).forGetter(Game::serverStartingMessage),
                Codec.STRING.optionalFieldOf("serverStartMessage", DEFAULT_SERVER_START_MESSAGE).forGetter(Game::serverStartMessage),
                Codec.STRING.optionalFieldOf("serverStopMessage", DEFAULT_SERVER_STOP_MESSAGE).forGetter(Game::serverStopMessage),
                Codec.STRING.optionalFieldOf("serverCrashMessage", DEFAULT_SERVER_CRASH_MESSAGE).forGetter(Game::serverCrashMessage),
                Codec.BOOL.optionalFieldOf("mirrorJoin", true).forGetter(Game::mirrorJoin),
                Codec.BOOL.optionalFieldOf("mirrorLeave", true).forGetter(Game::mirrorLeave),
                Codec.BOOL.optionalFieldOf("mirrorDeath", true).forGetter(Game::mirrorDeath),
                Codec.BOOL.optionalFieldOf("mirrorAdvancements", true).forGetter(Game::mirrorAdvancements),
                Codec.BOOL.optionalFieldOf("mirrorCommandMessages", true).forGetter(Game::mirrorCommandMessages)
        ).apply(instance, Game::new));

        public static final Game DEFAULT = new Game(
                DEFAULT_SERVER_STARTING_MESSAGE,
                DEFAULT_SERVER_START_MESSAGE,
                DEFAULT_SERVER_STOP_MESSAGE,
                DEFAULT_SERVER_CRASH_MESSAGE,
                DEFAULT_MIRROR_JOIN,
                DEFAULT_MIRROR_LEAVE,
                DEFAULT_MIRROR_DEATH,
                DEFAULT_MIRROR_ADVANCEMENTS,
                DEFAULT_MIRROR_COMMAND_MESSAGES
        );
    }

    public record Crashes(boolean uploadToMclogs) {
        public static final boolean DEFAULT_UPLOAD_TO_MCLOGS = true;

        public static final Codec<Crashes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("uploadToMclogs", DEFAULT_UPLOAD_TO_MCLOGS).forGetter(Crashes::uploadToMclogs)
        ).apply(instance, Crashes::new));

        public static final Crashes DEFAULT = new Crashes(DEFAULT_UPLOAD_TO_MCLOGS);
    }
}
