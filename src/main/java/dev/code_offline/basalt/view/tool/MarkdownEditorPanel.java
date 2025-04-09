package dev.code_offline.basalt.view.tool;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.core.Icons;
import dev.code_offline.basalt.model.Note;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MarkdownEditorPanel extends JPanel implements BasaltDockable {
    private static final int ICON_SIZE = 20;

    private final JTextArea inputArea;
    private final JEditorPane previewPane;
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    private final Note note;

    public MarkdownEditorPanel(Note note) {
        setLayout(new BorderLayout());

        this.note = note;

        var optionPanel = new JPanel();

        var cardLayout =  new CardLayout();

        var cardPanel = new JPanel(cardLayout);
        var editPanel = new JPanel(new BorderLayout());
        var previewPanel = new JPanel(new BorderLayout());
        var bothPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        inputArea = new JTextArea(note.getText());
        previewPane = new JEditorPane();

        var buttonGroup = new ButtonGroup();

        var editButton = toggleButton(Icons.EDIT.getIcon(ICON_SIZE));
        var previewButton = toggleButton(Icons.PREVIEW.getIcon(ICON_SIZE));
        var bothButton = toggleButton(Icons.STACK.getIcon(ICON_SIZE));

        buttonGroup.add(editButton);
        buttonGroup.add(previewButton);
        buttonGroup.add(bothButton);

        markdownParser = Parser.builder().build();
        htmlRenderer = HtmlRenderer.builder().build();

        // ввод
        inputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        var inputScroll = new JScrollPane(inputArea);

        // предпросмотр
        previewPane.setContentType("text/html");
        previewPane.setEditable(false);
        var previewScroll = new JScrollPane(previewPane);

        editButton.addActionListener((e) -> {
            cardLayout.show(cardPanel, "edit");

            editPanel.add(inputScroll);
        });
        previewButton.addActionListener((e) -> {
            cardLayout.show(cardPanel, "preview");

            previewPanel.add(previewScroll);
        });
        bothButton.addActionListener((e) -> {
            cardLayout.show(cardPanel, "both");

            bothPanel.add(inputScroll);
            bothPanel.add(previewScroll);
        });

        optionPanel.add(editButton);
        optionPanel.add(previewButton);
        optionPanel.add(bothButton);

        cardPanel.add(editPanel, "edit");
        cardPanel.add(previewPanel, "preview");
        cardPanel.add(bothPanel, "both");

        add(optionPanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        editButton.doClick();

        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updatePreview();
            }
        });

        updatePreview();
    }

    private JToggleButton toggleButton(Icon icon) {
        var button = new JToggleButton(icon);
        var buttonSize = (int) (ICON_SIZE * 1.5);

        button.setPreferredSize(new Dimension(buttonSize, buttonSize));

        return button;
    }

    private void updatePreview() {
        String markdownText = inputArea.getText();
        Node document = markdownParser.parse(markdownText);
        String html = htmlRenderer.render(document);
        previewPane.setText("<html><body>" + html + "</body></html>");
    }

    @Override
    public String getID() {
        return "markdown_editor";
    }

    @Override
    public String getTitle() {
        return note.getName();
    }

    @Override
    public Component getContent() {
        return this;
    }

    @Override
    public int getDockingModes() {
        return DockingMode.ALL;
    }

    @Override
    public ImageIcon getIconOriginal() {
        return Icons.EDIT_NOTE.getIcon();
    }
}
