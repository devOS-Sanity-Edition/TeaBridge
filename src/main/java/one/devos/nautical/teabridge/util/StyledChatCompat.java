package one.devos.nautical.teabridge.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;

// Uses the jankyiness of java to do styled chat support without a compile time dep
public class StyledChatCompat {
	private static MethodHandle METHOD = null;

	static {
		try {
			var lookup = MethodHandles.lookup();
			var extSignedMessage = lookup.findClass("eu.pb4.styledchat.ducks.ExtSignedMessage");
			METHOD = lookup.findVirtual(extSignedMessage, "styledChat_getArg", MethodType.methodType(Component.class, String.class));
		} catch (Throwable ignored) {
		}
	}

	private static boolean USE_COMPAT = true;

	public static Pair<String, @Nullable String> modify(PlayerChatMessage message) {
		if (USE_COMPAT) {
			try {
				String proxyContent = ((Component) METHOD.invoke(message, "proxy_content")).getString();
				String proxyDisplayName = ((Component) METHOD.invoke(message, "proxy_display_name")).getString();
				if (!proxyContent.isBlank() || !proxyDisplayName.isBlank())
					return Pair.of(proxyContent, proxyDisplayName);
			} catch (Throwable e) {
				USE_COMPAT = false;
			}
		}
		return Pair.of(message.signedContent(), null);
	}
}
