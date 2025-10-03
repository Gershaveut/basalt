package dev.code_offline.basalt.view.tool.folder;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class FolderTreeCellRenderer extends DefaultTreeCellRenderer {
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		var component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		component.setPreferredSize(new Dimension(9999, component.getPreferredSize().height)); // TODO: Создаёт проблемы при отстёгивании
		
		return component;
	}
}
