package one.devos.nautical.teabridge.mixin;

import java.io.File;

import one.devos.nautical.teabridge.TeaBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.CrashReport;
import one.devos.nautical.teabridge.util.CrashHandler;

@Mixin(CrashReport.class)
public abstract class CrashReportMixin {
    @Inject(method = "saveToFile", at = @At("HEAD"))
    private void uploadCrash(File file, CallbackInfoReturnable<Boolean> cir) {
        CrashHandler.CRASH_VALUE.crash(() -> {
            if (TeaBridge.config.crashes().uploadToMclogs()) CrashHandler.uploadAndSend((CrashReport) (Object) this);
        });
    }
}
