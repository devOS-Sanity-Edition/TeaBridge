package one.devos.nautical.chatlink.discord;

import javax.annotation.Nonnull;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import one.devos.nautical.chatlink.Config;

public class ChannelListener extends ListenerAdapter implements ServerStarted {
    public static ChannelListener INSTANCE = new ChannelListener();

    private MinecraftServer server;

    @Override
    public void onServerStarted(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        var author = event.getAuthor();
        if (!event.isFromGuild() || !event.getChannel().getId().equals(Config.INSTANCE.discord.channel) || author.isBot()) return;

        PlayerList playerList = server.getPlayerList();
        if (playerList != null) {
            var hoverText =
                Component.literal(author.getName()).append(
                Component.literal("#" + author.getDiscriminator()).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

            var member = event.getMember();
            if (member != null && member.getRoles().size() != 0) {
                hoverText.append(Component.literal("\n\nROLES").withStyle(ChatFormatting.BOLD));
                for (Role role : member.getRoles()) {
                    hoverText.append(Component.literal("\n■ ").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(role.getColorRaw()))).append(Component.literal(role.getName()).withStyle(ChatFormatting.GRAY)));
                }
            }

            var text = 
                Component.literal("@" + author.getName() + ": " + event.getMessage().getContentDisplay())
                .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)).withColor(ChatFormatting.GRAY));

            playerList.broadcastSystemMessage(text, false);
        }
    }
}
