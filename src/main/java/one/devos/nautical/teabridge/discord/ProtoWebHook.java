package one.devos.nautical.teabridge.discord;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import one.devos.nautical.teabridge.util.JsonUtils;
import org.jetbrains.annotations.Nullable;

public record ProtoWebHook(Supplier<String> username, Supplier<String> avatar) {
    public Message createMessage(String content, @Nullable String displayName) throws Exception {
        return new Message(content, AllowedMentions.INSTANCE, displayName != null ? displayName : this.username.get(), this.avatar.get());
    }

    public record Message(String content, AllowedMentions allowedMentions, String username, String avatarUrl) {
        public static final Codec<Message> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("content").forGetter(Message::content),
                AllowedMentions.CODEC.fieldOf("allowed_mentions").forGetter(Message::allowedMentions),
                Codec.STRING.fieldOf("username").forGetter(Message::username),
                Codec.STRING.fieldOf("avatar_url").forGetter(Message::avatarUrl)
        ).apply(instance, Message::new));

        public DataResult<String> toJson() {
            return CODEC.encodeStart(JsonOps.INSTANCE, this).map(JsonUtils.GSON::toJson);
        }
    }

    public record AllowedMentions(List<String> parse) {
        public static final AllowedMentions INSTANCE = new AllowedMentions(List.of());

        public static final Codec<AllowedMentions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.listOf().fieldOf("parse").forGetter(AllowedMentions::parse)
        ).apply(instance, AllowedMentions::new));

        public DataResult<String> toJson() {
            return CODEC.encodeStart(JsonOps.INSTANCE, this).map(JsonUtils.GSON::toJson);
        }
    }
}
