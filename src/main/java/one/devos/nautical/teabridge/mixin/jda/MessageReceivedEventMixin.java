package one.devos.nautical.teabridge.mixin.jda;

import java.util.concurrent.atomic.AtomicBoolean;

import org.spongepowered.asm.mixin.Mixin;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import one.devos.nautical.teabridge.duck.MessageEventProxyTag;

@Mixin(MessageReceivedEvent.class)
public abstract class MessageReceivedEventMixin implements MessageEventProxyTag {
    private AtomicBoolean teabridge$proxied = new AtomicBoolean();

    @Override
    public void teabridge$setProxied() {
        teabridge$proxied.set(true);
    }

    @Override
    public boolean teabridge$isProxied() {
        return teabridge$proxied.get();
    }
}
