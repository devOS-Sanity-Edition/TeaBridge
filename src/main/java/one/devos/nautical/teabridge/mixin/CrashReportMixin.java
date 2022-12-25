package one.devos.nautical.teabridge.mixin;

import java.io.File;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.CrashReport;
import one.devos.nautical.teabridge.Config;
import one.devos.nautical.teabridge.discord.Discord;
import one.devos.nautical.teabridge.util.CrashUploader;

@Mixin(CrashReport.class)
public abstract class CrashReportMixin {
    @Inject(method = "saveToFile", at = @At("RETURN"))
    private void teabridge$uploadCrash(File file, CallbackInfoReturnable<Boolean> cir) {
        if (Config.INSTANCE.crashes.uploadToMclogs) {
            CrashUploader.upload((CrashReport) (Object) this);
        } else {
            Discord.send(Config.INSTANCE.game.serverCrashMessage);
        }
    }
}
