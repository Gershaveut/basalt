package dev.code_offline.basalt.view.tool.folder;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

public class FolderTransferHandler extends TransferHandler {
    private DataFlavor flavor = new DataFlavor(DefaultMutableTreeNode.class, "Tree Node");

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree) c;
        TreePath path = tree.getSelectionPath();
        if (path == null) return null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        return new TransferableNode(node);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDataFlavorSupported(flavor);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) return false;
        try {
            Transferable t = support.getTransferable();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) t.getTransferData(flavor);
            JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
            TreePath dest = dl.getPath();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dest.getLastPathComponent();
            parent.add(new DefaultMutableTreeNode(node.getUserObject()));
            ((JTree) support.getComponent()).updateUI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private class TransferableNode implements Transferable {
        private DefaultMutableTreeNode node;

        public TransferableNode(DefaultMutableTreeNode node) {
            this.node = node;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{flavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor f) {
            return f.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor f) {
            return node;
        }
    }
}
