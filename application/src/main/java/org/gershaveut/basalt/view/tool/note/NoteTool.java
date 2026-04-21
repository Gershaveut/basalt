package org.gershaveut.basalt.view.tool.note;

import com.javadocking.dockable.DockingMode;
import jakarta.annotation.Nullable;
import org.aspectj.weaver.patterns.ThisOrTargetAnnotationPointcut;
import org.gershaveut.basalt.ApplicationUtil;
import org.gershaveut.basalt.view.ApplicationFrame;
import org.gershaveut.basalt.view.DebugModeListener;
import org.gershaveut.basalt.view.Icons;
import org.gershaveut.basalt.view.tool.AbstractTool;
import org.gershaveut.basalt_share.model.Comment;
import org.gershaveut.basalt_share.model.Note;
import org.gershaveut.basalt_share.model.Person;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.hibernate.boot.model.source.spi.EntityHierarchySource;
import org.springframework.data.util.Pair;
import org.springframework.data.web.PagedModel;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.function.Consumer;

public class NoteTool extends AbstractTool implements DebugModeListener {
    private final EventListenerList listeners = new EventListenerList();

    private static final int ICON_SIZE = 20;

    private final JTextArea inputArea;
    private final JEditorPane previewPane;
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    private JScrollPane commentsScrollPane;
    private JLabel pageLabel;
    private final JPopupMenu commentPopupMenu;

    private @Nullable Comment currentComment;

    private final JPanel optionPanel;

    private final Note note;

    private PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(0, 0, 0, 0);
    private long currentPage;

    private boolean saved;

    public NoteTool(Note note, String text, ApplicationFrame applicationFrame, Person clientPerson) {
        this.setLayout(new BorderLayout());

        this.setPreferredSize(ApplicationUtil.BOX_WINDOW_DIMENSION_TOOL);

        this.note = note;

        optionPanel = new JPanel();

        var cardLayout = new CardLayout();

        var cardPanel = new JPanel(cardLayout);
        var editPanel = new JPanel(new BorderLayout());
        var previewPanel = new JPanel(new BorderLayout());
        var bothPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        var commentsPanel = new JPanel(new BorderLayout());

        this.inputArea = new JTextArea(text);
        this.previewPane = new JEditorPane();
        this.commentsScrollPane = new JScrollPane(Box.createVerticalBox());
        this.pageLabel = new JLabel();
        
        var buttonGroup = new ButtonGroup();

        var editButton = getResizeButton(new JToggleButton(Icons.EDIT.getIcon(ICON_SIZE)));
        var previewButton = getResizeButton(new JToggleButton(Icons.PREVIEW.getIcon(ICON_SIZE)));
        var bothButton = getResizeButton(new JToggleButton(Icons.STACK.getIcon(ICON_SIZE)));
        var commentsButton = getResizeButton(new JToggleButton(Icons.FORUM.getIcon(ICON_SIZE)));
        var saveButton = getResizeButton(new JButton(Icons.SAVE.getIcon(ICON_SIZE)));

        saveButton.setEnabled(ApplicationUtil.accessNote(clientPerson, note));

        saveButton.setAlignmentX(Container.RIGHT_ALIGNMENT);

        buttonGroup.add(editButton);
        buttonGroup.add(previewButton);
        buttonGroup.add(bothButton);
        buttonGroup.add(commentsButton);

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
            save();
        });
        commentsButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "comments");

            this.commentsScrollPane = new JScrollPane(Box.createVerticalBox());

            openComments(currentPage);

            var optionPanel = new JPanel();

            pageLabel = new JLabel();

            var scrollEndButton = new JButton("↓");
            var firstButton = new JButton("<<");
            var previousButton = new JButton("<");
            var nextButton = new JButton(">");
            var lastButton = new JButton(">>");
            var createButton = new JButton("+");

            optionPanel.add(scrollEndButton);
            optionPanel.add(firstButton);
            optionPanel.add(previousButton);
            optionPanel.add(pageLabel);
            optionPanel.add(nextButton);
            optionPanel.add(lastButton);
            optionPanel.add(createButton);

            scrollEndButton.addActionListener(e1 -> {
                var verticalScrollBar = commentsScrollPane.getVerticalScrollBar();

                verticalScrollBar.setValue(verticalScrollBar.getMaximum());
            });
            
            firstButton.addActionListener(e1 -> {
                openComments(0);
            });

            previousButton.addActionListener(e1 -> {
                openComments(Math.max(0, currentPage - 1));
            });

            nextButton.addActionListener(e1 -> {
                openComments(Math.min(pageMetadata.totalPages() - 1, currentPage + 1));
            });

            lastButton.addActionListener(e1 -> {
                openComments(pageMetadata.totalPages() - 1);
            });

            createButton.addActionListener(e1 -> {
                var commentText = JOptionPane.showInputDialog(this, "Введите комментарий", "Создание комментария", JOptionPane.PLAIN_MESSAGE);

                if (commentText == null)
                    return;

                for (NoteListener listener : listeners.getListeners(NoteListener.class)) {
                    listener.addComment(commentText, pageMetadata.totalPages());
                }
            });

            commentsPanel.add(optionPanel, BorderLayout.SOUTH);
            commentsPanel.add(commentsScrollPane, BorderLayout.CENTER);
        });
        commentPopupMenu = new JPopupMenu();

        var openProfile = new JMenuItem("Открыть профиль");

        openProfile.addActionListener(e -> {
            for (NoteListener listener : listeners.getListeners(NoteListener.class)) {
                assert currentComment != null;
                listener.openProfile(currentComment.getPerson());
            }
        });

        commentPopupMenu.add(openProfile);

        optionPanel.add(editButton);
        optionPanel.add(previewButton);
        optionPanel.add(bothButton);
        optionPanel.add(commentsButton);
        optionPanel.add(saveButton);

        cardPanel.add(editPanel, "edit");
        cardPanel.add(previewPanel, "preview");
        cardPanel.add(bothPanel, "both");
        cardPanel.add(commentsPanel, "comments");

        add(optionPanel, BorderLayout.NORTH);
        add(cardPanel, BorderLayout.CENTER);

        editButton.doClick();

        ApplicationUtil.addDocumentListener(inputArea.getDocument(), () -> {
            updatePreview();
            setSaveIndicator(true);
        });

        ApplicationUtil.registerActionMap(inputArea, this.getID(), KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), this::save);

        updatePreview();

        applicationFrame.addDebugModeListener(this);
    }

    public void setComments(PagedModel<Comment> comments, long currentPage, Consumer<Pair<Long, Consumer<Person>>> getPerson) {
        this.currentPage = currentPage;
        this.pageMetadata = comments.getMetadata();
        
        pageLabel.setText(currentPage + " / " + (pageMetadata.totalPages() - 1));
        
        var commentsPanel = (JComponent) commentsScrollPane.getViewport().getView();
        
        commentsPanel.removeAll();

        comments.getContent().forEach(comment -> {
            var commentPanel = new JPanel(new BorderLayout());

            var headerPanel = new JPanel(new BorderLayout());
            var optionsButton = new JButton("...");

            optionsButton.addActionListener(e -> {
                currentComment = comment;
                commentPopupMenu.show(this, optionsButton.getX(), optionsButton.getY());
            });

            getPerson.accept(Pair.of(1L, (person) -> {
                headerPanel.add(new JLabel(person.getUsername()), BorderLayout.WEST);
            }));

            headerPanel.add(optionsButton, BorderLayout.EAST);

            commentPanel.add(headerPanel, BorderLayout.PAGE_START);

            var textArea = new JTextArea(comment.getText());
            textArea.setLineWrap(true);
            textArea.setEditable(false);

            commentPanel.add(textArea, BorderLayout.CENTER);

            if (comment.getLastUpdated() != null)
                commentPanel.add(new JLabel(comment.getLastUpdated().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))), BorderLayout.PAGE_END);

            commentsPanel.add(commentPanel);
            commentsPanel.add(new JSeparator());
        });
        
        var verticalScrollBar = commentsScrollPane.getVerticalScrollBar();

        verticalScrollBar.setValue(0);
    }

    private void openComments(long page) {
        for (NoteListener listener : listeners.getListeners(NoteListener.class)) {
            listener.openComments(page);
        }
    }

    private AbstractButton getResizeButton(AbstractButton button) {
        var buttonSize = (int) (ICON_SIZE * 1.5);

        button.setPreferredSize(new Dimension(buttonSize, buttonSize));

        return button;
    }

    private void setSaveIndicator(boolean visibly) {
        var newTitle = getTitle();

        if (visibly)
            newTitle = newTitle + "*";

        getDelegate().setTitle(newTitle);

        saved = !visibly;
    }

    private void updatePreview() {
        String markdownText = inputArea.getText();
        Node document = markdownParser.parse(markdownText);
        String html = htmlRenderer.render(document);
        previewPane.setText("<html><body>" + html + "</body></html>");
    }

    public void save() {
        if (!saved) {
            for (NoteListener listener : listeners.getListeners(NoteListener.class)) {
                listener.onSave(getText());
            }

            setSaveIndicator(false);
        }
    }

    public String getText() {
        return inputArea.getText();
    }

    public void addNoteListener(NoteListener noteListener) {
        listeners.add(NoteListener.class, noteListener);
    }

    public void removeNoteListener(NoteListener noteListener) {
        listeners.remove(NoteListener.class, noteListener);
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
