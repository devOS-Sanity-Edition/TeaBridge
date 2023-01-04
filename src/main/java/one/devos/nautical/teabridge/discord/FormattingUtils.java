package one.devos.nautical.teabridge.discord;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import one.devos.nautical.teabridge.TeaBridge;

public class FormattingUtils {
    public static MutableComponent formatUser(final boolean arrows, final User user, @Nullable final Member member) {
        var mention = Component.literal("@" + (member != null ? member.getEffectiveName() : user.getName()));

        var hoverText =
            Component.literal(user.getName()).append(
            Component.literal("#" + user.getDiscriminator()).withStyle(ChatFormatting.DARK_GRAY));

        if (member != null) {
            mention.withStyle(Style.EMPTY.withColor(TextColor.fromRgb(member.getColorRaw())));
            hoverText.append("\n\n").append(FormattingUtils.formatRoles(member.getRoles()));
        }

        var prefix = Component.empty();
        var suffix = Component.empty();
        if (arrows) {
            prefix = Component.literal("<");
            suffix = Component.literal(">");
        }

        return prefix.append(mention).append(suffix).withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));
    }

    public static MutableComponent formatRoles(final Collection<Role> roles) {
        var formatted = Component.literal("NO ROLES").withStyle(ChatFormatting.BOLD);

        if (roles.size() != 0) {
            formatted = Component.literal("ROLES").withStyle(ChatFormatting.BOLD);
            for (Role role : roles) {
                formatted.append(
                    Component.literal("\n■ ").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(role.getColorRaw()))).append(
                    Component.literal(role.getName()).withStyle(Style.EMPTY.withBold(false))));
            }
        }

        return formatted;
    }

    public static MutableComponent formatMessage(final Message message) {
        var formatted = Component.empty();

        // Handle non default message types
        switch (message.getType()) {
            case CHANNEL_PINNED_ADD:
                formatted
                    .append(Component.literal("[").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                    .append(formatUser(false, message.getAuthor(), message.getMember()).withStyle(ChatFormatting.ITALIC))
                    .append(Component.literal(" has pinned a message").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
                    .append(Component.literal("]").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                return formatted;
            case DEFAULT:
                break;
            default:
                TeaBridge.LOGGER.error("Message: " + message.getContentRaw() + " has a unknown message type: ", message.getType().toString());
                return formatted.append(Component.literal("Error when handling message, check log for details!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        }

        // Handle replying
        var referencedMessage = message.getReferencedMessage();
        if (referencedMessage != null) {
            formatted.append("Reply to ").append(formatMessage(referencedMessage).withStyle(ChatFormatting.GRAY)).append("\n");
        }

        var messageContent = PlaceholderAware.formatText(message.getContentDisplay());

        for (Attachment attachment : message.getAttachments()) {
            messageContent.append(
                Component.literal(" [" + attachment.getFileName() + "]")
                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, attachment.getUrl())).withColor(ChatFormatting.BLUE)));
        }

        formatted.append(formatUser(true, message.getAuthor(), message.getMember()).append(" ").append(messageContent));

        return formatted;
    }

    private static class PlaceholderAware {
        private static VarHandle markdownParser = null;
        private static MethodHandle parseNodes = null;

        private static MethodHandle literalNodeConstructor = null;

        private static MethodHandle parentNodeConstructor = null;
        private static MethodHandle toText = null;

        static {
            try {
                var lookup = MethodHandles.lookup();

                var textNodeClass = lookup.findClass("eu.pb4.placeholders.api.node.TextNode");
                var markdownParserClass = lookup.findClass("eu.pb4.placeholders.api.parsers.MarkdownLiteParserV1");
                markdownParser = lookup.findStaticVarHandle(markdownParserClass, "ALL", lookup.findClass("eu.pb4.placeholders.api.parsers.NodeParser"));
                parseNodes = lookup.findVirtual(markdownParserClass, "parseNodes", MethodType.methodType(textNodeClass.arrayType(), textNodeClass));

                var literalNodeClass = lookup.findClass("eu.pb4.placeholders.api.node.LiteralNode");
                literalNodeConstructor = lookup.findConstructor(literalNodeClass, MethodType.methodType(void.class, String.class));
 
                var parentNodeClass = lookup.findClass("eu.pb4.placeholders.api.node.parent.ParentNode");
                parentNodeConstructor = lookup.findConstructor(parentNodeClass, MethodType.methodType(void.class, textNodeClass.arrayType()));
                toText = lookup.findVirtual(parentNodeClass, "toText", MethodType.methodType(Component.class, lookup.findClass("eu.pb4.placeholders.api.ParserContext"), boolean.class));
            } catch (Exception e) { }
        }

        private static MutableComponent formatText(String text) {
            try {
                return ((Component) toText.invoke(parentNodeConstructor.invoke(parseNodes.invoke(markdownParser.getVolatile(), literalNodeConstructor.invoke(text))), null, true)).copy();
            } catch (Throwable e) { }
            return Component.literal(text);
        }
    }
}
