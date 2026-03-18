package org.gershaveut.basalt.view;

import com.javadocking.dockable.CompositeDockable;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.action.DefaultPopupMenuFactory;

import javax.swing.*;
import java.awt.*;

public class ApplicationPopupMenuFactory extends DefaultPopupMenuFactory {
    @Override
    public JPopupMenu createPopupMenu(Dockable selectedDockable, CompositeDockable compositeDockable) {
        var popupMenu = super.createPopupMenu(selectedDockable, compositeDockable);

        for (Component component : popupMenu.getComponents()) {
            if (component instanceof JMenuItem menuItem) {
                var name = menuItem.getText();

                name = switch (name) {
                    case "Close" -> "Закрыть";
                    case "Restore" -> "Восстановить";
                    case "Maximize" -> "Максимизировать";
                    case "Minimize" -> "Минимизировать";
                    case "Externalize" -> "Вынести";
                    case "Close All" -> "Закрыть всё";
                    case "Minimize All" -> "Минимизировать всё";
                    case "Restore All" -> "Восстановить всё";
                    case "Close Others" -> "Закрыть остальные";
                    case "Minimize Others" -> " остальные";
                    default -> name;
                };
                
                menuItem.setText(name);
            }
        }
        
        return popupMenu;
    }
}
