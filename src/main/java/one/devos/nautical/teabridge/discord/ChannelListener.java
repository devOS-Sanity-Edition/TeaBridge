package one.devos.nautical.teabridge.discord;

import java.util.Optional;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import one.devos.nautical.teabridge.PlatformUtil;
import one.devos.nautical.teabridge.util.FormattingUtils;

public class ChannelListener extends ListenerAdapter {
    public static final ChannelListener INSTANCE = new ChannelListener();

    private MinecraftServer server;
    private long channel;

    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    void setChannel(long channel) {
        this.channel = channel;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent receivedEvent) {
        PKCompat.await(receivedEvent, (event, proxied) -> {
            if (server == null) return;

            if (
                !event.isFromGuild() ||
                event.getChannel().getIdLong() != channel ||
                event.getAuthor().isBot() ||
                (event.isWebhookMessage() && !proxied) ||
                (!event.isWebhookMessage() && proxied)
            ) return;

            final var playerList = server.getPlayerList();

            Optional<MutableComponent> formattedMessage;
            try {
                formattedMessage = FormattingUtils.formatMessage(event.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                formattedMessage = Optional.of(PlatformUtil.literal("Exception when handling message, check log for details!").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            }
            if (playerList != null && formattedMessage.isPresent()) playerList.broadcastSystemMessage(formattedMessage.get(), false);
        });
    }
}
