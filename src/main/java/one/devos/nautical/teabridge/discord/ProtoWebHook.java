package one.devos.nautical.teabridge.discord;

import java.util.Optional;
import java.util.function.Supplier;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import one.devos.nautical.teabridge.util.JsonUtils;

public record ProtoWebHook(Supplier<String> username, Supplier<String> avatar) {
    public String jsonWithContent(final String content, Optional<String> displayName) throws Exception {
        return JsonUtils.GSON.toJson(new Json(content, AllowedMentions.INSTANCE, displayName.orElseGet(this.username), avatar.get()));
    }

    private static record Json(@Expose String content, @Expose @SerializedName("allowed_mentions") AllowedMentions allowedMentions, @Expose String username, @Expose @SerializedName("avatar_url") String avatarUrl) { }

    private static class AllowedMentions {
        static final AllowedMentions INSTANCE = new AllowedMentions();

        @Expose String[] parse = new String[0];
    }
}
