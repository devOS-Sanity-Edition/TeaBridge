package one.devos.nautical.teabridge.discord;

import java.util.Map;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class CommandUtils extends ListenerAdapter {
    static final CommandUtils INSTANCE = new CommandUtils();

    private final Map<String, Consumer<SlashCommandInteractionEvent>> COMMANDS = new Object2ReferenceOpenHashMap<>();

    void createCommand(Guild guild, CommandData commandData, Consumer<SlashCommandInteractionEvent> whenUsed) {
        COMMANDS.putIfAbsent(commandData.getName(), whenUsed);
        guild.updateCommands().addCommands(commandData).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (COMMANDS.containsKey(event.getName())) COMMANDS.get(event.getName()).accept(event);
    }
}
