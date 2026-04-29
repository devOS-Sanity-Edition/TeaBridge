package one.devos.nautical.teabridge.discord;

import java.util.function.Supplier;

public record WebhookPrototype(Supplier<String> username, Supplier<String> avatarUrl) {
	public WebhookPrototype withDisplayName(String name) {
		return new WebhookPrototype(() -> name, this.avatarUrl);
	}
}
