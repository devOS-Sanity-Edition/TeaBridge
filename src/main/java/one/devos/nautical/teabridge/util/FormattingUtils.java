package one.devos.nautical.teabridge.util;

import java.util.Collection;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.sticker.Sticker;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import one.devos.nautical.teabridge.PlatformUtil;
import one.devos.nautical.teabridge.TeaBridge;

public class FormattingUtils {
    public static MutableComponent formatUser(final boolean arrows, final User user, @Nullable final Member member) {
        var mention = PlatformUtil.literal("@" + (member != null ? member.getEffectiveName() : user.getEffectiveName()));

        var hoverText = PlatformUtil.literal("@" + user.getName());

        if (member != null) {
            mention.withStyle(Style.EMPTY.withColor(TextColor.fromRgb(member.getColorRaw())));
            hoverText.append("\n\n").append(FormattingUtils.formatRoles(member.getRoles()));
        }

        var prefix = PlatformUtil.empty();
        var suffix = PlatformUtil.empty();
        if (arrows) {
            prefix = PlatformUtil.literal("<");
            suffix = PlatformUtil.literal(">");
        }

        return prefix.append(mention.withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)))).append(suffix);
    }

    public static MutableComponent formatRoles(final Collection<Role> roles) {
        var formatted = PlatformUtil.literal("NO ROLES").withStyle(ChatFormatting.BOLD);

        if (roles.size() != 0) {
            formatted = PlatformUtil.literal("ROLES").withStyle(ChatFormatting.BOLD);
            for (Role role : roles) {
                formatted.append(
                    PlatformUtil.literal("\n■ ").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(role.getColorRaw()))).append(
                    PlatformUtil.literal(role.getName()).withStyle(Style.EMPTY.withBold(false))));
            }
        }

        return formatted;
    }

    public static Optional<MutableComponent> formatMessage(final Message message) throws Exception {
        var formatted = PlatformUtil.empty();

        // Handle non default message types
        switch (message.getType()) {
            case CHANNEL_PINNED_ADD:
                formatted
                    .append(PlatformUtil.literal("[").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                    .append(formatUser(false, message.getAuthor(), message.getMember()).withStyle(ChatFormatting.ITALIC))
                    .append(PlatformUtil.literal(" has pinned a message").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                    .append(PlatformUtil.literal("]").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                return Optional.of(formatted);
            case INLINE_REPLY:
                var referencedMessage = message.getReferencedMessage();
                if (referencedMessage != null) {
                    var formattedReferencedMessage = formatMessage(referencedMessage);
                    if (formattedReferencedMessage.isPresent()) formatted
                        .append("Reply to ")
                        .append(formattedReferencedMessage.get())
                        .append("\n");
                }
                break;
            case THREAD_CREATED:
            case AUTO_MODERATION_ACTION:
                return Optional.empty();
            case SLASH_COMMAND:
            case CONTEXT_COMMAND:
            case DEFAULT:
                break;
            default:
                TeaBridge.LOGGER.error("Message: " + message.getContentRaw() + " has a unknown message type: " + message.getType().toString());
                return Optional.empty();
        }

        var messageContent = PlatformUtil.formatText(message.getContentDisplay());

        for (Attachment attachment : message.getAttachments()) {
            messageContent.append(
                PlatformUtil.literal(" [" + attachment.getFileName() + "]")
                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, attachment.getUrl())).withColor(ChatFormatting.BLUE)));
        }

        for (Sticker sticker : message.getStickers()) {
            messageContent.append(
                PlatformUtil.literal(" [" + sticker.getName() + "]")
                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, sticker.getIconUrl())).withColor(ChatFormatting.BLUE)));
        }

        formatted.append(formatUser(true, message.getAuthor(), message.getMember()).append(" ").append(messageContent));

        return Optional.of(formatted);
    }

}
