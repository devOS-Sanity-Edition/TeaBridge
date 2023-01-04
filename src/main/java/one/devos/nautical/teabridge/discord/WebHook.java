package one.devos.nautical.teabridge.discord;

import java.util.function.Supplier;

import com.google.gson.annotations.Expose;

import one.devos.nautical.teabridge.util.JsonUtils;

public record WebHook(Supplier<String> username, Supplier<String> avatar) {
    public String jsonWithContent(final String content) throws Exception {
        return JsonUtils.toJsonString(this, json -> {
            return json
                .put("content", content)
                .put("allowed_mentions", AllowedMentions.INSTANCE)
                .put("username", username.get())
                .put("avatar_url", avatar.get());
        });
    }

    private static class AllowedMentions {
        static final AllowedMentions INSTANCE = new AllowedMentions();

        @Expose String[] parse = new String[0];
    }
}
