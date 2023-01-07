package one.devos.nautical.teabridge;

import java.nio.file.Path;
import java.util.function.Consumer;

import com.mojang.brigadier.CommandDispatcher;

import eu.pb4.placeholders.api.node.LiteralNode;
import eu.pb4.placeholders.api.node.parent.ParentNode;
import eu.pb4.placeholders.api.parsers.MarkdownLiteParserV1;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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

    public static MutableComponent formatText(String text) {
        return new ParentNode(MarkdownLiteParserV1.ALL.parseNodes(new LiteralNode(text))).toText(null, true).copy();
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
    }
}
