package dev.code_offline.basalt;

import dev.code_offline.basalt.model.note.NoteInfo;
import dev.code_offline.basalt_share.model.Note;
import dev.code_offline.basalt_share.model.Person;
import dev.code_offline.basalt_share.model.Role;
import org.apache.commons.text.WordUtils;
import org.dyn4j.geometry.Vector2;
import org.springframework.lang.Nullable;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


public final class ApplicationUtil {
    public static final Dimension BOX_WINDOW_DIMENSION_TOOL = new Dimension(500, 500);
    
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
        mapper.writer(SerializationFeature.INDENT_OUTPUT);
        
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
    
    public static void showErrorDialog(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void registerAccelerator(JMenuItem menuItem, JComponent component, KeyStroke keyStroke, @Nullable Runnable updateMenuContext) {
        menuItem.setAccelerator(keyStroke);
        
        registerActionMap(component, "menuAction_" + menuItem.getText(), keyStroke, () -> {
            if (updateMenuContext != null)
                updateMenuContext.run();
            
            if (menuItem.isEnabled() && menuItem.isVisible())
                menuItem.doClick();
        });
    }
    
    public static void registerActionMap(JComponent component, String name, KeyStroke keyStroke, Runnable runnable) {
        component.getInputMap(JComponent.WHEN_FOCUSED)
                .put(keyStroke, name);
        
        component.getActionMap()
                .put(name, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        runnable.run();
                    }
                });
    }
    
    public static void addDocumentListener(Document document, Runnable listener) {
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onDocumentUpdated();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                onDocumentUpdated();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
               onDocumentUpdated();
            }
            
            private void onDocumentUpdated() {
                listener.run();
            }
        });
    }
    
    public static String toDisplayName(String name) {
        return WordUtils.capitalize(name.toLowerCase().replace("_", " "));
    }
    
    public static String fromDisplayName(String displayName) {
        return displayName.toUpperCase().replace(" ", "_");
    }
}
