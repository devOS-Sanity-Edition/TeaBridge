package one.devos.nautical.teabridge.mixin;

import java.nio.file.Path;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.CrashReport;
import net.minecraft.ReportType;
import one.devos.nautical.teabridge.util.CrashHandler;

@Mixin(CrashReport.class)
public abstract class CrashReportMixin {
	@Inject(method = "saveToFile(Ljava/nio/file/Path;Lnet/minecraft/ReportType;Ljava/util/List;)Z", at = @At("HEAD"))
	private void handleCrash(Path path, ReportType reportType, List<String> list, CallbackInfoReturnable<Boolean> cir) {
		CrashHandler.handle((CrashReport) (Object) this);
	}
}
