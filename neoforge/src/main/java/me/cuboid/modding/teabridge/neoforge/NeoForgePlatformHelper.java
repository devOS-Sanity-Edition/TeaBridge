package me.cuboid.modding.teabridge.neoforge;

import net.neoforged.fml.loading.FMLPaths;
import me.cuboid.modding.teabridge.PlatformHelper;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements PlatformHelper {
	@Override
	public Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}
}
