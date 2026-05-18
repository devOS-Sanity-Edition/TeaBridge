package me.cuboid.modding.teabridge.fabric;

import net.fabricmc.loader.api.FabricLoader;
import me.cuboid.modding.teabridge.PlatformHelper;

import java.nio.file.Path;

public class FabricPlatformHelper implements PlatformHelper {
	@Override
	public Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}
}
