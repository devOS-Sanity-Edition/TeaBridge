package one.devos.nautical.teabridge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.loader.api.FabricLoader;

public record Config(
		Discord discord,
		Avatars avatars,
		Game game,
		Crashes crashes
) {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setStrictness(Strictness.LENIENT).disableHtmlEscaping().create();

	public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Discord.CODEC.fieldOf("discord").forGetter(Config::discord),
			Avatars.CODEC.fieldOf("avatars").forGetter(Config::avatars),
			Game.CODEC.fieldOf("game").forGetter(Config::game),
			Crashes.CODEC.fieldOf("crashes").forGetter(Config::crashes)
	).apply(instance, Config::new));

	public static final Config DEFAULT = new Config(Discord.DEFAULT, Avatars.DEFAULT, Game.DEFAULT, Crashes.DEFAULT);
	public static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve(TeaBridge.ID + ".json");

	public static DataResult<Config> load() {
		try {
			if (Files.exists(PATH)) {
				try (BufferedReader reader = Files.newBufferedReader(PATH)) {
					return CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader));
				}
			} else {
				try (BufferedWriter writer = Files.newBufferedWriter(PATH, StandardOpenOption.CREATE)) {
					GSON.toJson(CODEC.encodeStart(JsonOps.INSTANCE, DEFAULT).getOrThrow(), writer);
					return DataResult.success(DEFAULT);
				}
			}
		} catch (Exception e) {
			return DataResult.error(e::getMessage);
		}
	}

	public record Discord(
			String token,
			String webhook,
			int pkMessageDelay,
			boolean pkMessageDelayMilliseconds
	) {
		public static final String DEFAULT_TOKEN = "";
		public static final String DEFAULT_WEBHOOK = "";
		public static final int DEFAULT_PK_MESSAGE_DELAY = 0;
		public static final boolean DEFAULT_PK_MESSAGE_DELAY_MILLISECONDS = true;

		public static final Codec<Discord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("token").forGetter(Discord::token),
				Codec.STRING.fieldOf("webhook").forGetter(Discord::webhook),
				Codec.INT.fieldOf("pkMessageDelay").forGetter(Discord::pkMessageDelay),
				Codec.BOOL.fieldOf("pkMessageDelayMilliseconds").forGetter(Discord::pkMessageDelayMilliseconds)
		).apply(instance, Discord::new));

		public static final Discord DEFAULT = new Discord(
				DEFAULT_TOKEN,
				DEFAULT_WEBHOOK,
				DEFAULT_PK_MESSAGE_DELAY,
				DEFAULT_PK_MESSAGE_DELAY_MILLISECONDS
		);
	}

	public record Avatars(String avatarUrl, boolean useTextureId) {
		public static final String DEFAULT_AVATAR_URL = "https://api.nucleoid.xyz/skin/face/256/%s";
		public static final boolean DEFAULT_USE_TEXTURE_ID = false;

		public static final Codec<Avatars> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("avatarUrl").forGetter(Avatars::avatarUrl),
				Codec.BOOL.fieldOf("useTextureId").forGetter(Avatars::useTextureId)
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
				Codec.STRING.fieldOf("serverStartingMessage").forGetter(Game::serverStartingMessage),
				Codec.STRING.fieldOf("serverStartMessage").forGetter(Game::serverStartMessage),
				Codec.STRING.fieldOf("serverStopMessage").forGetter(Game::serverStopMessage),
				Codec.STRING.fieldOf("serverCrashMessage").forGetter(Game::serverCrashMessage),
				Codec.BOOL.fieldOf("mirrorJoin").forGetter(Game::mirrorJoin),
				Codec.BOOL.fieldOf("mirrorLeave").forGetter(Game::mirrorLeave),
				Codec.BOOL.fieldOf("mirrorDeath").forGetter(Game::mirrorDeath),
				Codec.BOOL.fieldOf("mirrorAdvancements").forGetter(Game::mirrorAdvancements),
				Codec.BOOL.fieldOf("mirrorCommandMessages").forGetter(Game::mirrorCommandMessages)
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
				Codec.BOOL.fieldOf("uploadToMclogs").forGetter(Crashes::uploadToMclogs)
		).apply(instance, Crashes::new));

		public static final Crashes DEFAULT = new Crashes(DEFAULT_UPLOAD_TO_MCLOGS);
	}
}
