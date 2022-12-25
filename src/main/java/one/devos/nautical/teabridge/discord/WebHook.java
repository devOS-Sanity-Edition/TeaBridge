package one.devos.nautical.teabridge.discord;

import java.util.HashMap;
import java.util.function.Supplier;

import com.google.common.collect.Maps;
import com.google.gson.annotations.Expose;

public record WebHook(Supplier<String> username, String avatar) {
    static final HashMap<String, Object> CONVERTING_MAP = Maps.newHashMap();

    static class AllowedMentions {
        static final AllowedMentions INSTANCE = new AllowedMentions();

        @Expose String[] parse = new String[0];
    }
}
