package one.devos.nautical.teabridge.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.jetbrains.annotations.Nullable;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;

@FunctionalInterface
public interface StyledChatCompat {
	StyledChatCompat INSTANCE = Util.make(() -> {
		try {
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			Class<?> extSignedMessage = lookup.findClass("eu.pb4.styledchat.ducks.ExtSignedMessage");
			MethodHandle getArg = lookup.findStatic(extSignedMessage, "getArg", MethodType.methodType(Component.class, PlayerChatMessage.class, String.class));

			return (message, id) -> {
				try {
					//noinspection OverlyStrongTypeCast
					String arg = ((Component) getArg.invokeExact(message, id)).getString();
					return arg.isBlank() ? null : arg;
				} catch (Throwable ignored) {
					return null;
				}
			};
		} catch (Throwable ignored) {
			return (message, id) -> null;
		}
	});

	String PROXY_CONTENT_ARG = "proxy_content";
	String PROXY_DISPLAY_NAME_ARG = "proxy_display_name";

	@Nullable
	String getArg(PlayerChatMessage message, String id);
}
