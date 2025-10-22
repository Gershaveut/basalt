package dev.code_offline.basalt.view;

import dev.code_offline.basalt.core.Icons;

import javax.swing.*;
import java.awt.*;

public class AboutFrame extends JFrame {
    private static int LOGO_SIZE = 300;

    public AboutFrame() {
        var panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        this.setTitle("О программе");
        this.setLayout(new BorderLayout());
        this.setSize(300, 400);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        panel.add(new JLabel(new ImageIcon(Icons.BASALT.getRawIcon().getImage().getScaledInstance(LOGO_SIZE, (int) (LOGO_SIZE * 1.25), 0))));
        panel.add(new JLabel("Basalt"));
        panel.add(new JLabel("Code-offline"));

        this.add(panel, BorderLayout.CENTER);
    }
}
