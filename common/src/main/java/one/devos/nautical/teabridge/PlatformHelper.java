package one.devos.nautical.teabridge;

import java.nio.file.Path;
import java.util.ServiceLoader;

public interface PlatformHelper {
	PlatformHelper INSTANCE = ServiceLoader.load(PlatformHelper.class)
			.findFirst()
			.orElseThrow(() -> new NullPointerException("Failed to load platform helper"));

	Path getConfigDir();
}
