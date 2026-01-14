package dev.code_offline.basalt.view.start;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class StartTransferHandler extends TransferHandler {
    private final DataFlavor flavor = DataFlavor.stringFlavor;
    private final EventListenerList listeners;

    public StartTransferHandler(EventListenerList listeners) {
        this.listeners = listeners;
    }
    
    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDataFlavorSupported(flavor);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) return false;

        try {
            var file = (String) support.getTransferable().getTransferData(flavor);

            for (StartListener listener : listeners.getListeners(StartListener.class)) {
                listener.openDatabase(file);
            }
            
            return true;
        } catch (UnsupportedFlavorException | IOException ignored) {
            return false;
        }
    }
}
