package dev.code_offline.basalt.view;

import dev.code_offline.basalt.Main;
import dev.code_offline.basalt.core.Icons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Locale;

public class AboutFrame extends JFrame {
    private static final int LOGO_SIZE = 65;

    public AboutFrame() {
        this.setTitle("О программе");
        this.setSize(375, 200);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        this.setResizable(false);
        
        var panel = new JPanel();
        var textPanel = Box.createVerticalBox();
       
        var logo = new JLabel(new ImageIcon(Icons.BASALT.getRawIcon().getImage().getScaledInstance(LOGO_SIZE, (int) (LOGO_SIZE * 1.25), 0)));
        
        logo.setVerticalAlignment(JLabel.TOP);
        
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 15, 0, 0));
       
        textPanel.setBorder(new EmptyBorder(0, 15, 0, 0));
        
        textPanel.add(new JLabel(Main.APP_NAME));
        textPanel.add(new JLabel("Code-offline"));
        textPanel.add(Box.createVerticalStrut(15));
        textPanel.add(new JLabel("Версия программы: " + Main.APP_VERSION));
        textPanel.add(new JLabel("Версия клиента: " + Main.NETWORK_VERSION));
        
        panel.add(logo, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);
        
        this.add(panel);
    }
}
