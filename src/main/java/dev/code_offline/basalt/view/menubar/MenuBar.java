package dev.code_offline.basalt.view.menubar;

import dev.code_offline.basalt.view.AboutFrame;
import dev.code_offline.basalt.view.settings.SettingsFrame;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.util.Arrays;
import java.util.function.Consumer;

public class MenuBar extends JMenuBar {
    private final EventListenerList listeners = new EventListenerList();

    private final AboutFrame aboutFrame = new AboutFrame();
    private final SettingsFrame settingsFrame = new SettingsFrame();

    public MenuBar() {
        var fileMenu = new JMenu("Файл");
        var helpMenu = new JMenu("Помощь");

        fileMenu.add(menuItem("Новый файл", () -> notifyListeners(MenuBarListener::newFile)));
        fileMenu.add(menuItem("Закрыть проект", () -> notifyListeners(MenuBarListener::closeProject)));
        fileMenu.addSeparator();
        fileMenu.add(menuItem("Настройки", this::settings));
        fileMenu.addSeparator();
        fileMenu.add(menuItem("Сохранить", () -> notifyListeners(MenuBarListener::save)));
        fileMenu.addSeparator();
        fileMenu.add(menuItem("Выход", this::exit));

        helpMenu.add(menuItem("О программе", this::about));

        add(fileMenu);
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

    private void notifyListeners(Consumer<MenuBarListener> action) {
        Arrays.stream(listeners.getListeners(MenuBarListener.class)).toList().forEach(action);
    }

    private void exit() {
        System.exit(0);
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
