package dev.code_offline.basalt.view;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame(JPanel panel) throws HeadlessException {
        this.setSize(600,600);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.add(panel, BorderLayout.CENTER);

        this.setVisible(true);
    }
}
