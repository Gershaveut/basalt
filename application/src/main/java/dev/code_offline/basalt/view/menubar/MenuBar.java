package dev.code_offline.basalt.view.menubar;

import dev.code_offline.basalt.ApplicationUtil;
import dev.code_offline.basalt.model.settings.Theme;
import dev.code_offline.basalt.view.AboutFrame;
import dev.code_offline.basalt.view.settings.SettingsFrame;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.function.Consumer;

public class MenuBar extends JMenuBar {
    private final EventListenerList listeners = new EventListenerList();

    private final AboutFrame aboutFrame = new AboutFrame();
    private final SettingsFrame settingsFrame = new SettingsFrame();

    public MenuBar() {
        var fileMenu = new JMenu("Файл");
        var viewMenu = new JMenu("Вид");
        var helpMenu = new JMenu("Помощь");

        fileMenu.add(menuItem("Закрыть проект", () -> notifyListeners(MenuBarListener::closeProject)));
        fileMenu.addSeparator();
        fileMenu.add(menuItem("Настройки", this::settings, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK)));
        fileMenu.addSeparator();
        fileMenu.add(menuItem("Сохранить всё", () -> notifyListeners(MenuBarListener::save), KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK)));
        fileMenu.addSeparator();
        fileMenu.add(menuItem("Выход", this::exit));

        var themeMenu = new JMenu("Тема");
        
        for (Theme theme : Theme.values()) {
            themeMenu.add(menuItem(theme.toString(), theme::applyTheme));
        }
        
        viewMenu.add(themeMenu);
        
        helpMenu.add(menuItem("О программе", this::about));

        add(fileMenu);
        add(viewMenu);
        add(helpMenu);
    }

    public void addMenuBarListener(MenuBarListener menuBarListener) {
        listeners.add(MenuBarListener.class, menuBarListener);
    }

    public void removeMenuBarListener(MenuBarListener menuBarListener) {
        listeners.remove(MenuBarListener.class, menuBarListener);
    }

    private JMenuItem menuItem(String name, Runnable action) {
        var menuItem = new JMenuItem(name);

        menuItem.addActionListener((e) -> action.run());
        
        return menuItem;
    }
    
    private JMenuItem menuItem(String name, Runnable action, KeyStroke keyStroke) {
        var menuItem = menuItem(name, action);
        
        ApplicationUtil.registerAccelerator(menuItem, this, keyStroke, null);
        
        return menuItem;
    }
    
    private void notifyListeners(Consumer<MenuBarListener> action) {
        Arrays.stream(listeners.getListeners(MenuBarListener.class)).toList().forEach(action);
    }

    private void exit() {
        notifyListeners(MenuBarListener::exit);
    }

    private void about() {
        aboutFrame.setVisible(true);
    }

    private void settings() {
        settingsFrame.setVisible(true);
    }

    public SettingsFrame getSettingsFrame() {
        return settingsFrame;
    }
}
