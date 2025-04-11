package dev.code_offline.basalt.view.menubar;

import dev.code_offline.basalt.view.AboutFrame;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.util.Arrays;
import java.util.function.Consumer;

public class MenuBar extends JMenuBar {
    private final EventListenerList listeners = new EventListenerList();

    private final AboutFrame aboutFrame = new AboutFrame();

    public MenuBar() {
        var fileMenu = new JMenu("Файл");
        var helpMenu = new JMenu("Помощь");

        fileMenu.add(menuItem("Новый файл", () -> notifyAll(MenuBarListener::newFile)));
        fileMenu.add(menuItem("Закрыть проект", () -> notifyAll(MenuBarListener::closeProject)));
        fileMenu.addSeparator();
        fileMenu.add(menuItem("Сохранить", () -> notifyAll(MenuBarListener::save)));
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

    private void notifyAll(Consumer<MenuBarListener> action) {
        Arrays.stream(listeners.getListeners(MenuBarListener.class)).toList().forEach(action);
    }

    private void exit() {
        System.exit(0);
    }

    private void about() {
        aboutFrame.setVisible(true);
    }
}
