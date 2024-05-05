package one.devos.nautical.teabridge;

import java.nio.file.Path;
import java.util.function.Consumer;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class PlatformUtil implements DedicatedServerModInitializer {
    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static MutableComponent empty() {
        return Component.empty();
    }

    public static MutableComponent literal(String text) {
        return Component.literal(text);
    }

    public static MutableComponent translatable(String key) {
        return Component.translatable(key);
    }

    // private static MutableComponent formatMarkdownNodes(List<MarkdownNode> nodes) {
    //     var formatted = empty();
    //     for (MarkdownNode node : nodes) {
    //         System.out.println(node);
    //         if (node instanceof SymbolNode symbolNode) {
    //             var formattedChildren = formatMarkdownNodes(symbolNode.getNodes()); // this variable name is cursed
    //             System.out.println(symbolNode.getLeft());
    //             System.out.println(symbolNode.getRight());
    //             switch (symbolNode.getLeft()) {
    //                 case "**":
    //                     formattedChildren = formattedChildren.withStyle(ChatFormatting.BOLD);
    //                     break;
    //                 case "`":
    //                 case "``":
    //                 case "```":
    //                     formattedChildren = formattedChildren.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    //                     break;
    //                 case "||":
    //                     formattedChildren = (literal("[").append(translatable("options.hidden")).append("]"))
    //                         .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, formattedChildren)))
    //                         .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    //                     break;
    //                 case "*":
    //                 case "_":
    //                     formattedChildren = formattedChildren.withStyle(ChatFormatting.ITALIC);
    //                     break;
    //                 case "__":
    //                     formattedChildren = formattedChildren.withStyle(ChatFormatting.UNDERLINE);
    //                     break;
    //                 case "~~":
    //                     formattedChildren = formattedChildren.withStyle(ChatFormatting.STRIKETHROUGH);
    //                     break;
    //                 default:
    //                     break;
    //             }
    //             formatted = formatted.append(formattedChildren);
    //         } else if (node instanceof MentionNode mentionNode) {
    //             formatted = formatted.append(node.toString());
    //         } else {
    //             formatted = formatted.append(node.toString());
    //         }
    //     }
    //     return formatted;
    // }

    public static MutableComponent formatText(String text) {
        // var parser = MarkdownParser.Companion.getGlobalInstance();
        // var root = parser.parse(text);
        // return formatMarkdownNodes(root.getNodes());
        return literal(text);
    }

    public static void registerCommand(Consumer<CommandDispatcher<CommandSourceStack>> consumer) {
        CommandRegistrationCallback.EVENT.register((dispatcher, ctx, environment) -> consumer.accept(dispatcher));
    }


    @Override
    public void onInitializeServer() {
        TeaBridge.initialize();
        ServerLifecycleEvents.SERVER_STARTING.register(TeaBridge::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(TeaBridge::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPED.register(TeaBridge::onServerStop);

        var phaseId = new ResourceLocation("teabridge", "mirror");
        ServerMessageEvents.CHAT_MESSAGE.addPhaseOrdering(new ResourceLocation("switchy_proxy", "set_args"), phaseId);
        ServerMessageEvents.CHAT_MESSAGE.addPhaseOrdering(phaseId, new ResourceLocation("switchy_proxy", "clear"));
        ServerMessageEvents.CHAT_MESSAGE.register(phaseId, TeaBridge::onChatMessage);

        ServerMessageEvents.COMMAND_MESSAGE.register(TeaBridge::onCommandMessage);
    }
}
