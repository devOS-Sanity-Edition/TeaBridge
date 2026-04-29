package one.devos.nautical.teabridge.fabric;

import net.fabricmc.loader.api.FabricLoader;
import one.devos.nautical.teabridge.PlatformHelper;

import java.nio.file.Path;

public class FabricPlatformHelper implements PlatformHelper {
	@Override
	public Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}
}
