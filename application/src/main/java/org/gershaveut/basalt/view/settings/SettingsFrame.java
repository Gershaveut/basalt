package org.gershaveut.basalt.view.settings;

import org.gershaveut.basalt.ApplicationUtil;
import org.gershaveut.basalt.model.settings.SettingsModel;
import org.gershaveut.basalt.model.settings.SettingsTab;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

public class SettingsFrame extends JFrame {
    private final EventListenerList listeners = new EventListenerList();

    private @Nullable SettingsModel model;
    private @Nullable SettingsModel revertSettingsModel;

    private final JList<SettingsTab> settingsMenu;
    private final JPanel settingsTab;

    private final CardLayout settingsTabLayout;

    public SettingsFrame() {
        this.setTitle("Настройки");
        this.setLayout(new BorderLayout());
        this.setSize(500, 650);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        var splitPanel = new JSplitPane();
        var actionPanel = new JPanel();

        splitPanel.setDividerLocation(150);
        
        settingsTabLayout = new CardLayout();

        settingsMenu = new JList<>();
        settingsTab = new JPanel(settingsTabLayout);

        settingsMenu.addListSelectionListener(_ -> {
            if (!settingsMenu.isSelectionEmpty())
                settingsTabLayout.show(settingsTab, settingsMenu.getSelectedValue().getName());
        });
        SwingUtilities.invokeLater(() -> settingsMenu.setSelectedIndex(0));
        
        splitPanel.setLeftComponent(settingsMenu);
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
            cancel();
            
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
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
                
                setVisible(false);
            }
        });
    }
    
    private void cancel() {
        notifyListeners((listener) -> {
            assert revertSettingsModel != null;
            listener.revertSettings(revertSettingsModel);
        });
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

        var modelList = new DefaultListModel<SettingsTab>();
        
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

                    if (setting.getValue() != null && setting.getDefaultValue().getClass() == setting.getValue().getClass()) {
                        value = setting.getValue();
                    }
                    
                    var field = new JTextField();
                    
                    if (setting.isHideValue())
                        field = new JPasswordField();
                    
                    field.setMaximumSize(new Dimension(200, 30));
                    
                    var finalField = field;
                    ApplicationUtil.addDocumentListener(field.getDocument(), () -> {
                        notifyListeners(settingsListener -> settingsListener.setValue(setting.getName(), finalField.getText()));
                    });
                    
                    switch (value) {
                        case String s:
                            field.setText(s);

                            settingBox.add(field);
                            break;
                        case Boolean b:
                            var checkbox = new JCheckBox();

                            checkbox.setSelected(b);
                            checkbox.addActionListener(e -> {
                                notifyListeners(settingsListener -> settingsListener.setValue(setting.getName(), checkbox.isSelected()));
                            });

                            settingBox.add(checkbox);
                            break;
                        case Enum<?> e:
                            var constants = e.getClass().getEnumConstants();
                            var comboBox = new JComboBox<>(constants);
                            
                            comboBox.setMaximumSize(new Dimension(200, 30));
                            
                            comboBox.setSelectedIndex(Arrays.stream(constants).filter(constant -> Objects.equals(constant.toString(), e.toString())).findFirst().orElseThrow().ordinal());
                            comboBox.addItemListener(event -> {
                                notifyListeners(settingsListener -> settingsListener.setValue(setting.getName(), event.getItem()));
                            });
                            
                            settingBox.add(comboBox);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + setting.getDefaultValue());
                    }
                    
                    categoryPanel.add(settingPanel);
                });

                tabPanel.add(categoryPanel);
            });

            menuButton.addActionListener(e -> {
            });

            var scrollTab = new JScrollPane(tabPanel);
            scrollTab.setBorder(null);
            scrollTab.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            settingsTab.add(scrollTab, tab.getName());
            settingsMenu.add(menuButton);
            modelList.addElement(tab);
        });

        settingsMenu.setModel(modelList);
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
