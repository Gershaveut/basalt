package org.gershaveut.basalt.view.tool.folder;

import org.gershaveut.basalt.model.file.SFile;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Objects;

public class DirectoryTransferHandler extends TransferHandler {
    private final DataFlavor flavor = new DataFlavor(TransferableFile.class, "Tree Node");
    private final EventListenerList listeners;

    public DirectoryTransferHandler(EventListenerList listeners) {
        this.listeners = listeners;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree) c;

        TreePath path = Objects.requireNonNull(tree.getSelectionPath());
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

        return new TransferableFile(node);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDataFlavorSupported(flavor) && ((DefaultMutableTreeNode) ((JTree.DropLocation) support.getDropLocation()).getPath().getLastPathComponent()).getUserObject() instanceof SFile;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) return false;

        try {
            var file = (SFile) ((TransferableFile) support.getTransferable().getTransferData(flavor)).getFile();
            var targetDirectory = (SFile) ((DefaultMutableTreeNode) ((JTree.DropLocation) support.getDropLocation()).getPath().getLastPathComponent()).getUserObject();
         
            for (FilesListener listener : listeners.getListeners(FilesListener.class)) {
                listener.moveFile(file.getId(), targetDirectory.getPath());
            }

            return true;
        } catch (UnsupportedFlavorException | IOException ignored) {
            return false;
        }
    }

    private class TransferableFile implements Transferable {
        private final SFile file;

        public TransferableFile(DefaultMutableTreeNode node) {
            this.file = (SFile) node.getUserObject();
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
            return this;
        }

        public Object getFile() {
            return file;
        }
    }
}
