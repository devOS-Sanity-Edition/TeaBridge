package one.devos.nautical.teabridge.discord;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.GatewayIntent;
import one.devos.nautical.teabridge.Config;
import one.devos.nautical.teabridge.TeaBridge;
import one.devos.nautical.teabridge.util.MoreCodecs;

public class Discord {
	private static Supplier<Member> selfMember;
	private static JDA jda;
	private static boolean initialized = false;

	public static final WebHookPrototype WEB_HOOK = new WebHookPrototype(
			() -> selfMember().getEffectiveName(),
			() -> URI.create(selfMember().getEffectiveAvatarUrl())
	);

	public static void onConfigLoad(Config.Discord config) {
		stop();

		if (config.token().isEmpty()) {
			TeaBridge.LOGGER.error("Unable to load, no Discord token is specified!");
			return;
		}

		if (config.webhook().toString().isEmpty()) {
			TeaBridge.LOGGER.error("Unable to load, no Discord webhook is specified!");
			return;
		}

		try {
			// Get required data from webhook
			HttpResponse<String> response = TeaBridge.CLIENT.send(HttpRequest.newBuilder(config.webhook())
					.GET()
					.build(), HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() / 100 != 2)
				throw new Exception("Non-success status code from request " + response);
			WebHookData webHookData = WebHookData.fromJson(JsonParser.parseString(response.body())).getOrThrow();
			if (TeaBridge.config.debug()) TeaBridge.LOGGER.warn("Webhook response : {}", response.body());
			ChannelListener.INSTANCE.setChannel(webHookData.channelId);

			jda = JDABuilder.createDefault(config.token())
					.enableIntents(GatewayIntent.MESSAGE_CONTENT)
					.addEventListeners(ChannelListener.INSTANCE)
					.build();

			selfMember = Suppliers.memoize(() -> {
				Guild guild = jda.getGuildById(webHookData.guildId);
				if (guild != null) {
					return guild.getSelfMember();
				} else {
					throw new RuntimeException("Guild is null. This most likely means you are missing the message content intent, please enable it within the app's settings in the discord developer portal.");
				}
			});

			PKCompat.initIfEnabled();

			initialized = true;
		} catch (Throwable e) {
			TeaBridge.LOGGER.error("Exception initializing Discord", e);
		}
	}

	public static Member selfMember() {
		return selfMember.get();
	}

	public static boolean isInitialized() {
		return initialized;
	}

	public static void send(String message) {
		if (!initialized)
			return;
		send(WEB_HOOK.createMessage(message));
	}

	public static void send(WebHookPrototype.Message message) {
		if (!initialized)
			return;
		CompletableFuture.runAsync(() -> {
			try {
				HttpResponse<String> response = TeaBridge.CLIENT.send(HttpRequest.newBuilder(TeaBridge.config.discord().webhook())
						.POST(HttpRequest.BodyPublishers.ofString(message.toJson().getOrThrow()))
						.header("Content-Type", "application/json; charset=utf-8")
						.build(), HttpResponse.BodyHandlers.ofString());
				if (response.statusCode() / 100 != 2)
					throw new Exception("Non-success status code from request " + response);
			} catch (Exception e) {
				TeaBridge.LOGGER.warn("Failed to send webhook message to discord : ", e);
			}
		});
	}

	public static void stop() {
		initialized = false;
		if (jda != null) {
			jda.shutdown();
			jda = null;
		}
	}

	private record WebHookData(long guildId, long channelId) {
		public static final Codec<WebHookData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				MoreCodecs.SNOWFLAKE.fieldOf("guild_id").forGetter(WebHookData::guildId),
				MoreCodecs.SNOWFLAKE.fieldOf("channel_id").forGetter(WebHookData::channelId)
		).apply(instance, WebHookData::new));

		public static DataResult<WebHookData> fromJson(JsonElement json) {
			return CODEC.parse(JsonOps.INSTANCE, json);
		}
	}
}
