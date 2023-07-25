package one.devos.nautical.teabridge.discord;

import java.util.function.Supplier;

import com.google.gson.annotations.Expose;

import one.devos.nautical.teabridge.util.JsonUtils;

public record WebHook(Supplier<String> username, Supplier<String> avatar) {
    public String jsonWithContent(final String content) throws Exception {
        return JsonUtils.GSON.toJson(new Json(content, AllowedMentions.INSTANCE, username.get(), avatar.get()));
    }

    private static record Json(@Expose String content, @Expose AllowedMentions allowedMentions, @Expose String username, @Expose String avatar_url) { }

    private static class AllowedMentions {
        static final AllowedMentions INSTANCE = new AllowedMentions();

        @Expose String[] parse = new String[0];
    }
}
