package dev.code_offline.basalt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.code_offline.basalt_share.model.Note;
import dev.code_offline.basalt.model.note.NoteInfo;
import dev.code_offline.basalt_share.model.Person;
import dev.code_offline.basalt_share.model.Role;
import org.dyn4j.geometry.Vector2;
import org.springframework.lang.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


public final class ApplicationUtil {
    public static Vector2 pointToVector(Point point) {
        return new Vector2(point.x, point.y);
    }

    public static String assetsPrefix(String path) {
        return "assets/" + path;
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
    
    public static boolean hasRole(@Nullable Person person, Role role) {
        if (person == null)
            return false;
        
        return person.getRole().ordinal() >= role.ordinal();
    }
    
    public static boolean accessNote(@Nullable Person person, long noteAuthor) {
        if (person == null)
            return false;
        
        return hasRole(person, Role.MEMBER) && person.getId() == noteAuthor || hasRole(person, Role.MODERATOR);
    }
    
    public static boolean accessNote(@Nullable Person person, Note note) {
        return accessNote(person, note.getPerson());
    }
    
    public static boolean accessNote(@Nullable Person person, NoteInfo note) {
        return accessNote(person, note.getPerson());
    }
    
    public static boolean anyComponentsVisible(JComponent component) {
        return Arrays.stream(component.getComponents()).anyMatch(Component::isVisible);
    }
}
