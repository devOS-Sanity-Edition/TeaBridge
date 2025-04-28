package one.devos.nautical.teabridge.util;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.sticker.Sticker;
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
		var mention = Component.literal("@" + (member != null ? member.getEffectiveName() : user.getEffectiveName()));

		var hoverText = Component.literal("@" + user.getName());

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

		return prefix.append(mention.withStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(hoverText)))).append(suffix);
	}

	public static MutableComponent formatRoles(final Collection<Role> roles) {
		var formatted = Component.literal("NO ROLES").withStyle(ChatFormatting.BOLD);

		if (!roles.isEmpty()) {
			formatted = Component.literal("ROLES").withStyle(ChatFormatting.BOLD);
			for (Role role : roles) {
				formatted.append(
						Component.literal("\n■ ").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(role.getColorRaw()))).append(
								Component.literal(role.getName()).withStyle(Style.EMPTY.withBold(false))));
			}
		}

		return formatted;
	}

	public static Optional<MutableComponent> formatMessage(final Message message) {
		var formatted = Component.empty();

		// Handle non default message types
		switch (message.getType()) {
			case CHANNEL_PINNED_ADD:
				formatted
						.append(Component.literal("[").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
						.append(formatUser(false, message.getAuthor(), message.getMember()).withStyle(ChatFormatting.ITALIC))
						.append(Component.literal(" has pinned a message").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))
						.append(Component.literal("]").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
				return Optional.of(formatted);
			case INLINE_REPLY:
				var referencedMessage = message.getReferencedMessage();
				if (referencedMessage != null) {
					var formattedReferencedMessage = formatMessage(referencedMessage);
					formattedReferencedMessage.ifPresent(mutableComponent -> formatted
							.append("Reply to ")
							.append(mutableComponent)
							.append("\n"));
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
				TeaBridge.LOGGER.error("Message: {} has a unknown message type: {}", message.getContentRaw(), message.getType());
				return Optional.empty();
		}

		var messageContent = Component.literal(message.getContentDisplay());

		for (Attachment attachment : message.getAttachments()) {
			messageContent.append(
					Component.literal(" [" + attachment.getFileName() + "]")
							.withStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create(attachment.getUrl()))).withColor(ChatFormatting.BLUE)));
		}

		for (Sticker sticker : message.getStickers()) {
			messageContent.append(
					Component.literal(" [" + sticker.getName() + "]")
							.withStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(URI.create(sticker.getIconUrl()))).withColor(ChatFormatting.BLUE)));
		}

		formatted.append(formatUser(true, message.getAuthor(), message.getMember()).append(" ").append(messageContent));

		return Optional.of(formatted);
	}

}
