package one.devos.nautical.teabridge.neoforge;

import net.neoforged.fml.loading.FMLPaths;
import one.devos.nautical.teabridge.PlatformHelper;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements PlatformHelper {
	@Override
	public Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}
}
