package dev.code_offline.basalt.view.tool.folder;

import dev.code_offline.basalt.core.client.Client;
import dev.code_offline.basalt.model.Folder;
import dev.code_offline.basalt.model.note.NoteNode;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Objects;

public class FolderTransferHandler extends TransferHandler {
    private final DataFlavor flavor = new DataFlavor(TransferableFile.class, "Tree Node");
    
    private final Client client;
    
    public FolderTransferHandler(Client client) {
        this.client = client;
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
        return support.isDataFlavorSupported(flavor) && ((DefaultMutableTreeNode)((JTree.DropLocation) support.getDropLocation()).getPath().getLastPathComponent()).getUserObject() instanceof Folder;
	}

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) return false;
        
        try {
            var file = support.getTransferable().getTransferData(flavor);
            var targetFolder = (Folder) ((DefaultMutableTreeNode)((JTree.DropLocation) support.getDropLocation()).getPath().getLastPathComponent()).getUserObject();
          
            if (file instanceof NoteNode note) {
                client.moveNote(note.getId(), targetFolder);
            } else {
                client.moveFolder(((Folder) file).getPath(), targetFolder);
            }
            
            return true;
        } catch (UnsupportedFlavorException | IOException ignored) {
			return false;
		}
	}

    private class TransferableFile implements Transferable {
        private @Nullable Folder folder = null;
        private @Nullable NoteNode note = null;

        public TransferableFile(DefaultMutableTreeNode node) {
            var nodeContent = node.getUserObject();
            
            if (nodeContent instanceof NoteNode noteNode) {
                this.note = noteNode;
            } else {
                this.folder = (Folder) nodeContent;
            }
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
            if (folder != null) {
                return folder;
            } else {
				assert note != null;
				return note;
            }
        }
    }
}
