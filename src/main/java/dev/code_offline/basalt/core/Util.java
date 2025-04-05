package dev.code_offline.basalt.core;

import org.dyn4j.geometry.Vector2;

import java.awt.*;

public class Util {
    public static Vector2 pointToVector(Point point) {
        return new Vector2(point.x, point.y);
    }

    public static String assetsPrefix(String path) {
        return "src/main/resources/assets/" + path;
    }
}
