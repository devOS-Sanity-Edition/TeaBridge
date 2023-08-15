package one.devos.nautical.teabridge.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;

// Uses the junkyiness of java to do styled chat support without a compile time dep
public class StyledChatCompat {
    private static MethodHandle METHOD = null;
    static {
        try {
            var lookup = MethodHandles.lookup();
            var extSignedMessage = lookup.findClass("eu.pb4.styledchat.ducks.ExtSignedMessage");
            METHOD = lookup.findVirtual(extSignedMessage, "styledChat_getArg", MethodType.methodType(Component.class, String.class));
        } catch (Throwable e) { }
    };
    private static boolean USE_COMPAT = true;

    public static Pair<String, Optional<String>> modify(PlayerChatMessage message) {
        if (USE_COMPAT) {
            try {
                var proxyContent = ((Component) METHOD.invoke(message, "proxy_content")).getString();
                var proxyDisplayName = ((Component) METHOD.invoke(message, "proxy_display_name")).getString();
                if (!proxyContent.isBlank() || !proxyDisplayName.isBlank())
                    return Pair.of(proxyContent, Optional.of(proxyDisplayName));
            } catch (Throwable e) {
                USE_COMPAT = false;
            }
        }
        return Pair.of(message.signedContent(), Optional.empty());
    }
}
