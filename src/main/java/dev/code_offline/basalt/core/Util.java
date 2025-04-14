package dev.code_offline.basalt.core;

import org.dyn4j.geometry.Vector2;

import java.awt.*;
import java.util.List;
import java.util.Objects;


public class Util {
    public static Vector2 pointToVector(Point point) {
        return new Vector2(point.x, point.y);
    }

    public static <Target> List<Target> castList(List<?> sourceList, Class<Target> targetClass) {
        return sourceList.stream()
                .filter(item -> item == null || targetClass.isInstance(item))
                .map(item -> Objects.requireNonNull(targetClass.cast(item)))
                .toList();
    }

    public static String assetsPrefix(String path) {
        return "resources/assets/" + path;
    }
}
