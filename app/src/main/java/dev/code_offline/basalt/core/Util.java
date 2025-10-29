package dev.code_offline.basalt.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.code_offline.basalt.Main;
import org.dyn4j.geometry.Vector2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


public class Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);
    
    public static Vector2 pointToVector(Point point) {
        return new Vector2(point.x, point.y);
    }

    public static String assetsPrefix(String path) {
        return "assets/" + path;
    }
    
    public static String savePrefix(String path) {
        var appName = Main.APP_NAME;
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
                var configHome = System.getenv("XDG_CONFIG_HOME");
                
                if (configHome == null || configHome.isEmpty())
                    configHome = Paths.get(home, ".config").toString();
                
                configPath = Paths.get(configHome, appName);
                
                break;
        }
		
		try {
			Files.createDirectories(configPath);
		} catch (IOException exception) {
            LOGGER.error("Error create config directory", exception);
		}
        
        return configPath + "/" + path;
	}
    
    public static <T> void foreachNonList(Supplier<Integer> count, Function<Integer, T> getter, Consumer<T> action) {
        for (int i = 0; i < count.get(); i++) {
            try {
                action.accept(getter.apply(i));
            } catch (IndexOutOfBoundsException ignored) {
            
            }
        }
    }
    
    public static boolean isContextKey(KeyEvent e) {
        return e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU;
    }
    
    public static boolean isDeleteKey(KeyEvent e) {
        return e.getKeyCode() == KeyEvent.VK_DELETE;
    }
    
    public static ObjectMapper getMapper() {
        var mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        return mapper;
    }
}
