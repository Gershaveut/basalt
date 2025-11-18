package dev.code_offline.basalt_share;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Util {
	private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);
	
	public static final String APPLICATION_NAME = "basalt";
	
	public static final byte NETWORK_VERSION = 3;
	public static final double APPLICATION_VERSION = 0.4;
	
	public static String savePrefix(String path) {
		var appName = APPLICATION_NAME;
		var os = System.getProperty("os.name").toLowerCase();
		var home = System.getProperty("user.home");
		Path configPath;
		
		switch (os) {
			case "win":
				var appData = System.getenv("APPDATA");
				
				if (appData == null)
					appData = Paths.get(home, "AppData", "Roaming").toString();
				
				configPath = Paths.get(appData, appName);
				
				break;
			case "mac":
				configPath = Paths.get(home, "Library", "Application Support", appName);
				
				break;
			default:
				var dataHome = System.getenv("XDG_DATA_HOME");
				
				if (dataHome == null || dataHome.isEmpty())
					dataHome = Paths.get(home, ".local", "share").toString();
				
				configPath = Paths.get(dataHome, appName);
				
				break;
		}
		
		try {
			Files.createDirectories(configPath);
		} catch (IOException exception) {
			LOGGER.error("Error create config directory", exception);
		}
		
		return configPath + "/" + path;
	}
}
