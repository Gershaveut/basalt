package dev.code_offline.basalt.view.input;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;

public class InputTextFrame extends JFrame {
    private final EventListenerList listeners = new EventListenerList();

    public InputTextFrame(String title, String labelText, String inputText) {
        this.setTitle(title);
        this.setLayout(new BorderLayout());
        this.setSize(450, 150);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        var buttonPanel = new JPanel();

        var label = new JLabel(labelText);
        var input = new JTextField(inputText);

        var confirm = new JButton("ОК");
        var cancel = new JButton("Отмена");

        confirm.addActionListener(e -> {
            for (InputListener listener : listeners.getListeners(InputListener.class)) {
                listener.confirm(input.getText());
            }

            this.setVisible(false);
        });
        cancel.addActionListener(e -> {
            for (InputListener listener : listeners.getListeners(InputListener.class)) {
                listener.cancel();
            }

            this.setVisible(false);
        });

        buttonPanel.add(confirm);
        buttonPanel.add(cancel);

        add(label, BorderLayout.NORTH);
        add(input, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public InputTextFrame(String title, String labelText) {
        this(title, labelText, "");
    }

    public void addInputListener(InputListener inputListener) {
        listeners.add(InputListener.class, inputListener);
    }

    public void removeInputListener(InputListener inputListener) {
        listeners.remove(InputListener.class, inputListener);
    }
}
