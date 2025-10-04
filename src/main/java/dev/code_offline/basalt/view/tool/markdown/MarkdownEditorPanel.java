package dev.code_offline.basalt.view.tool.markdown;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.core.Icons;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.view.DebugModeListener;
import dev.code_offline.basalt.view.MainFrame;
import dev.code_offline.basalt.view.tool.BasaltDockable;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MarkdownEditorPanel extends JPanel implements BasaltDockable, DebugModeListener {
    private final EventListenerList listeners = new EventListenerList();

    private static final int ICON_SIZE = 20;

    private final JTextArea inputArea;
    private final JEditorPane previewPane;
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    private final JPanel optionPanel;
    
    private final Note note;

    public MarkdownEditorPanel(Note note, MainFrame mainFrame) {
        setLayout(new BorderLayout());

        this.note = note;

        optionPanel = new JPanel();

        var cardLayout =  new CardLayout();

        var cardPanel = new JPanel(cardLayout);
        var editPanel = new JPanel(new BorderLayout());
        var previewPanel = new JPanel(new BorderLayout());
        var bothPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        inputArea = new JTextArea(note.getText());
        previewPane = new JEditorPane();

        var buttonGroup = new ButtonGroup();

        var editButton = getResizeButton(new JToggleButton(Icons.EDIT.getIcon(ICON_SIZE)));
        var previewButton = getResizeButton(new JToggleButton(Icons.PREVIEW.getIcon(ICON_SIZE)));
        var bothButton = getResizeButton(new JToggleButton(Icons.STACK.getIcon(ICON_SIZE)));
        var saveButton = getResizeButton(new JButton(Icons.SAVE.getIcon(ICON_SIZE)));

        saveButton.setAlignmentX(Container.RIGHT_ALIGNMENT);

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

        editButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "edit");

            editPanel.add(inputScroll);
        });
        previewButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "preview");

            previewPanel.add(previewScroll);
        });
        bothButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "both");

            bothPanel.add(inputScroll);
            bothPanel.add(previewScroll);
        });
        saveButton.addActionListener(e -> {
            for (MarkdownListener listener : listeners.getListeners(MarkdownListener.class)) {
                listener.onSave(getText());
            }
        });

        optionPanel.add(editButton);
        optionPanel.add(previewButton);
        optionPanel.add(bothButton);
        optionPanel.add(saveButton);

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
        
        mainFrame.addDebugModeListener(this);
    }

    private AbstractButton getResizeButton(AbstractButton button) {
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

    public String getText() {
        return inputArea.getText();
    }

    public void addMarkdownListener(MarkdownListener markdownListener) {
        listeners.add(MarkdownListener.class, markdownListener);
    }

    public void removeMarkdownListener(MarkdownListener markdownListener) {
        listeners.remove(MarkdownListener.class, markdownListener);
    }

    @Override
    public String getID() {
        return "markdown_editor " + note.getId();
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
    
    @Override
    public void debugEnabled() {
        var debugText = new JLabel("Id: " + note.getId());
        
        optionPanel.add(debugText);
    }
}
