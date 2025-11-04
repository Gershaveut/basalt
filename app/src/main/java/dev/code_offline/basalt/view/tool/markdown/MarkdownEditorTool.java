package dev.code_offline.basalt.view.tool.markdown;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.Util;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.person.Person;
import dev.code_offline.basalt.view.BasaltFrame;
import dev.code_offline.basalt.view.DebugModeListener;
import dev.code_offline.basalt.view.Icons;
import dev.code_offline.basalt.view.tool.AbstractTool;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MarkdownEditorTool extends AbstractTool implements DebugModeListener {
	private final EventListenerList listeners = new EventListenerList();
	
	private static final int ICON_SIZE = 20;
	
	private final JTextArea inputArea;
	private final JEditorPane previewPane;
	private final Parser markdownParser;
	private final HtmlRenderer htmlRenderer;
	
	private final JPanel optionPanel;
	
	private final Note note;
	
	public MarkdownEditorTool(Note note, BasaltFrame basaltFrame, Person clientPerson) {
		setLayout(new BorderLayout());
		
		this.note = note;
		
		optionPanel = new JPanel();
		
		var cardLayout = new CardLayout();
		
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
		
		saveButton.setVisible(Util.accessNote(clientPerson, note));
		
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
			save();
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
				setSaveIndicator(true);
			}
		});
		
		updatePreview();
		
		basaltFrame.addDebugModeListener(this);
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
	}
	
	private void updatePreview() {
		String markdownText = inputArea.getText();
		Node document = markdownParser.parse(markdownText);
		String html = htmlRenderer.render(document);
		previewPane.setText("<html><body>" + html + "</body></html>");
	}
	
	public void save() {
		for (MarkdownListener listener : listeners.getListeners(MarkdownListener.class)) {
			listener.onSave(getText());
		}
		
		setSaveIndicator(false);
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
