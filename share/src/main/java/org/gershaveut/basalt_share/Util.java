package org.gershaveut.basalt_share;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Util {
	private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);
	
	public static final String APPLICATION_NAME = "basalt";
	public static final String APPLICATION_FORMAT = ".basalt";
	
	public static final byte NETWORK_VERSION = 5;
	public static final String APPLICATION_VERSION = "0.6";
	
    public static String savePrefix(String path) {
		var appName = APPLICATION_NAME;
		var home = SystemUtils.USER_HOME;
		Path configPath;
	    
        if (SystemUtils.IS_OS_WINDOWS) {
            var appData = System.getenv("APPDATA");
            
            if (appData == null)
                appData = Paths.get(home, "AppData", "Roaming").toString();

            configPath = Paths.get(appData, appName);
        } else if (SystemUtils.IS_OS_MAC) {
            configPath = Paths.get(home, "Library", "Application Support", appName);
        } else {
            var dataHome = System.getenv("XDG_DATA_HOME");

            if (dataHome == null || dataHome.isEmpty())
                dataHome = Paths.get(home, ".local", "share").toString();

            configPath = Paths.get(dataHome, appName);
        }
		
		try {
			Files.createDirectories(configPath);
		} catch (IOException exception) {
			LOGGER.error("Error create config directory", exception);
		}
		
		return configPath + "/" + path;
	}
	
	public static Pair<String, String> splitAbsolutePath(String absolutePath, String slash) {
		var lastSlashIndex = absolutePath.lastIndexOf(slash) + 1;

		var name = absolutePath.substring(lastSlashIndex);
		var path = absolutePath.substring(0, lastSlashIndex);

		return Pair.of(name, path);
	}
}
