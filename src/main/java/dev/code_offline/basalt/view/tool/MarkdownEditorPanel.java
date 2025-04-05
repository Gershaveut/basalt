package dev.code_offline.basalt.view.tool;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.core.Icons;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MarkdownEditorPanel extends JPanel implements BasaltDockable {
    private final JTextArea inputArea;
    private final JEditorPane previewPane;
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    public MarkdownEditorPanel() {
        setLayout(new GridLayout(1, 2, 10, 0));

        markdownParser = Parser.builder().build();
        htmlRenderer = HtmlRenderer.builder().build();

        // ввод
        inputArea = new JTextArea("# Заголовок \n\n**Полужирное** \n\n _Курсив_");
        inputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane inputScroll = new JScrollPane(inputArea);

        // предпросмотр
        previewPane = new JEditorPane();
        previewPane.setContentType("text/html");
        previewPane.setEditable(false);
        JScrollPane previewScroll = new JScrollPane(previewPane);

        add(inputScroll);
        add(previewScroll);

        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updatePreview();
            }
        });

        updatePreview();
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

    public void setText(String content) {
        inputArea.setText(content);
        updatePreview();
    }

    @Override
    public String getID() {
        return "markdown_editor";
    }

    @Override
    public String getTitle() {
        return "Редактор";
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
