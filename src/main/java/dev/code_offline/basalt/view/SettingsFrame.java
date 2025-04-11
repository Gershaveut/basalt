package dev.code_offline.basalt.view;

import dev.code_offline.basalt.model.settings.SettingsModel;

import javax.swing.*;
import java.awt.*;

public class SettingsFrame extends JFrame {
    private SettingsModel model;

    private JPanel settingsMenu;
    private JPanel settingsTab;

    private CardLayout settingsTabLayout;

    public SettingsFrame() {
        this.setTitle("Настройки");
        this.setLayout(new BorderLayout());
        this.setSize(500, 650);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        var splitPanel = new JSplitPane();

        settingsTabLayout = new CardLayout();

        settingsMenu = new JPanel();
        settingsTab = new JPanel(settingsTabLayout);

        splitPanel.setLeftComponent(settingsMenu);
        splitPanel.setRightComponent(settingsTab);

        add(splitPanel);
    }

    public SettingsFrame(SettingsModel model) {
        this();
        loadSettings();
    }

    private void loadSettings() {
        settingsTab.removeAll();

        model.getSettingsTabs().forEach(tab -> {
            var menuButton = new JButton(tab.getName());
            var tabPanel = new JPanel();
            tabPanel.setLayout(new BoxLayout(tabPanel, BoxLayout.Y_AXIS));

            tabPanel.add(new JLabel(tab.getName()));

            tab.getSettingsCategories().forEach(category -> {
                var categoryPanel = new JPanel();
                categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));

                categoryPanel.add(new JLabel(category.getName()));
                categoryPanel.add(new JSeparator());

                category.getSettings().forEach(setting -> {
                    var settingPanel = new JPanel();

                    settingPanel.add(new JLabel(setting.getName()));

                    switch (setting.getDefaultValue()) {
                        case String s:
                            var field = new JTextField();

                            field.setText(s);
                            field.addActionListener(e -> {
                                setting.setValue(field.getText());
                            });

                            settingPanel.add(field);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + setting.getDefaultValue());
                    }

                    categoryPanel.add(settingPanel);
                });

                tabPanel.add(categoryPanel);
            });

            menuButton.addActionListener(e -> {
                settingsTabLayout.show(settingsTab, tab.getName());
            });

            settingsTab.add(tabPanel, tabPanel.getName());
            settingsMenu.add(menuButton);
        });
    }

    public void setModel(SettingsModel model) {
        this.model = model;
        loadSettings();
    }
}
