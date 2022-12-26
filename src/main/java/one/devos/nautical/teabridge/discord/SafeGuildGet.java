package one.devos.nautical.teabridge.discord;

import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Sets;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import one.devos.nautical.teabridge.TeaBridge;

class SafeGuildGet {
    private static final Set<Runnable> queued = Sets.newHashSet();
    private static Guild cached;

    static void set(JDA jda) {
        new Thread(() -> {
            var tries = 0;
            while (cached == null) {
                if (tries >= 100) {
                    TeaBridge.LOGGER.fatal("Failed to get guild!");
                    break;
                }
                try {
                    cached = jda.getGuilds().get(0);
                } catch (IndexOutOfBoundsException e) { }
                tries++;
            }
            for (Runnable runnable : queued) {
                runnable.run();
            }
            queued.clear();
        }).start();
    }

    static void safeGet(Consumer<Guild> on) {
        if (cached != null) {
            on.accept(cached);
        } else {
            queued.add(() -> {
                on.accept(cached);
            });
        }
    }
}
