package dev.code_offline.basalt.view.settings;

import dev.code_offline.basalt.model.settings.SettingsModel;
import org.springframework.lang.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.util.Arrays;
import java.util.function.Consumer;

public class SettingsFrame extends JFrame {
    private final EventListenerList listeners = new EventListenerList();

    private @Nullable SettingsModel model;
    private @Nullable SettingsModel revertSettingsModel;

    private final JPanel settingsMenu;
    private final JPanel settingsTab;

    private final CardLayout settingsTabLayout;

    public SettingsFrame() {
        this.setTitle("Настройки");
        this.setLayout(new BorderLayout());
        this.setSize(500, 650);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        var splitPanel = new JSplitPane();
        var actionPanel = new JPanel();

        settingsTabLayout = new CardLayout();

        settingsMenu = new JPanel();
        settingsTab = new JPanel(settingsTabLayout);

        settingsMenu.setLayout(new BoxLayout(settingsMenu, BoxLayout.Y_AXIS));

        var scrollMenu = new JScrollPane(settingsMenu);
        scrollMenu.setBorder(null);
        scrollMenu.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        splitPanel.setLeftComponent(scrollMenu);
        splitPanel.setRightComponent(settingsTab);

        var okButton = new JButton("ОК");
        var cancelButton = new JButton("Отмена");
        var applyButton = new JButton("Применить");

        okButton.addActionListener(e -> {
            notifyListeners((listener) -> {
                assert revertSettingsModel != null;
                listener.saveSettings(revertSettingsModel);
            });

            this.setVisible(false);
        });

        cancelButton.addActionListener(e -> {
            notifyListeners((listener) -> {
                assert revertSettingsModel != null;
                listener.revertSettings(revertSettingsModel);
            });

            this.setVisible(false);
        });

        applyButton.addActionListener(e -> {
            notifyListeners((listener) -> {
                assert revertSettingsModel != null;
                listener.saveSettings(revertSettingsModel);
            });
        });

        actionPanel.add(okButton);
        actionPanel.add(cancelButton);
        actionPanel.add(applyButton);

        actionPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        add(splitPanel);
        add(actionPanel, BorderLayout.SOUTH);
    }

    public SettingsFrame(SettingsModel model) {
        this();

        setModel(model);
    }

    private JSeparator getSeparator() {
        var separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        return separator;
    }

    private JLabel createDescriptionLabel(@Nullable String text) {
        var label = new JLabel(text);

        label.setFont(label.getFont().deriveFont(Font.ITALIC));

        return label;
    }

    public void setModel(SettingsModel model) {
        this.model = model;

        settingsMenu.removeAll();
        settingsTab.removeAll();

        var tabBorder = new EmptyBorder(0, 15, 0, 0);

        model.getSettingsTabs().forEach(tab -> {
            var menuButton = new JButton(tab.getName());
            var tabPanel = Box.createVerticalBox();
            var tabInfoPanel = Box.createVerticalBox();
            menuButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

            tabPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            tabInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            tabInfoPanel.setBorder(tabBorder);

            tabInfoPanel.add(new JLabel(tab.getName()));
            tabInfoPanel.add(createDescriptionLabel(tab.getDescription()));

            tabPanel.add(tabInfoPanel);
            tabPanel.add(getSeparator());

            tab.getSettingsCategories().forEach(category -> {
                var categoryPanel = Box.createVerticalBox();
                var categoryInfoPanel = Box.createVerticalBox();
                categoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                categoryInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                categoryInfoPanel.setBorder(tabBorder);

                categoryInfoPanel.add(new JLabel(category.getName()));
                categoryInfoPanel.add(createDescriptionLabel(category.getDescription()));

                categoryPanel.add(categoryInfoPanel);
                categoryPanel.add(getSeparator());

                category.getSettings().forEach(setting -> {
                    var settingPanel = Box.createVerticalBox();
                    var settingBox = Box.createHorizontalBox();
                    settingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    settingBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                    settingPanel.setBorder(new EmptyBorder(0, 25, 0 , 0));

                    settingBox.add(new JLabel(setting.getName()));

                    settingPanel.add(settingBox);
                    settingPanel.add(createDescriptionLabel(setting.getDescription()));

                    var value = setting.getDefaultValue();

                    if (setting.getValue() != null) {
                        value = setting.getValue();
                    }

                    switch (value) {
                        case String s:
                            var field = new JTextField();
                            field.setMaximumSize(new Dimension(200, settingBox.getPreferredSize().height));

                            field.setText(s);
                            field.addActionListener(e -> {
                                setting.setValue(field.getText());
                            });

                            settingBox.add(field);
                            break;
                        case Boolean b:
                            var checkbox = new JCheckBox();

                            checkbox.setSelected(b);
                            checkbox.addActionListener(e -> {
                                setting.setValue(checkbox.isSelected());
                            });

                            settingBox.add(checkbox);
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

            var scrollTab = new JScrollPane(tabPanel);
            scrollTab.setBorder(null);
            scrollTab.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            settingsTab.add(scrollTab, tab.getName());
            settingsMenu.add(menuButton);
        });
    }

    public void addSettingsListener(SettingsListener settingsListener) {
        listeners.add(SettingsListener.class, settingsListener);
    }

    public void removeSettingsListener(SettingsListener settingsListener) {
        listeners.remove(SettingsListener.class, settingsListener);
    }

    private void notifyListeners(Consumer<SettingsListener> action) {
        Arrays.stream(listeners.getListeners(SettingsListener.class)).toList().forEach(action);
    }

    @Override
    public void setVisible(boolean b) {
        if (b && model != null) {
            revertSettingsModel = model.clone();
        }

        super.setVisible(b);
    }
}
