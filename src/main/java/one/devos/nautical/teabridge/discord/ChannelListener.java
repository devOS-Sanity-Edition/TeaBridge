package one.devos.nautical.teabridge.discord;

import java.util.Optional;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import one.devos.nautical.teabridge.Config;
import one.devos.nautical.teabridge.PlatformUtil;

public class ChannelListener extends ListenerAdapter {
    public static ChannelListener INSTANCE = new ChannelListener();

    private MinecraftServer server;

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (server == null) return;

        final var author = event.getAuthor();
        if (!event.isFromGuild() || !event.getChannel().getId().equals(Config.INSTANCE.discord.channel) || author.isBot()) return;

        final var playerList = server.getPlayerList();

        Optional<MutableComponent> formattedMessage;
        try {
            formattedMessage = FormattingUtils.formatMessage(event.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            formattedMessage = Optional.of(PlatformUtil.literal("Exception when handling message, check log for details!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        }
        if (playerList != null && formattedMessage.isPresent()) playerList.broadcastSystemMessage(formattedMessage.get(), false);
    }
}
