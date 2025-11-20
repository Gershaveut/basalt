package dev.code_offline.basalt.view.tool.person;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt_share.model.Person;
import dev.code_offline.basalt.view.Icons;
import dev.code_offline.basalt.view.tool.AbstractTool;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PersonProfileTool extends AbstractTool {
	private final Person person;
	
	public PersonProfileTool(Person person) {
		this.setLayout(new BorderLayout());
		
		this.person = person;
		
		var infoPanel = Box.createVerticalBox();
		var usernameLabel = new JLabel("Имя пользователя: " + person.getUsername());
		var roleLabel = new JLabel("Роль: " + person.getRole().name);
		var descriptionLabel = new JLabel("Описание:");
		var descriptionTextArea = new JTextArea(person.getDescription());
		
		descriptionTextArea.setEditable(false);
		
		infoPanel.add(usernameLabel);
		infoPanel.add(roleLabel);
		infoPanel.add(descriptionLabel);
		infoPanel.add(new JScrollPane(descriptionTextArea));
		
		infoPanel.setBorder(new EmptyBorder(0, 15, 15, 15));
		
		this.add(infoPanel, BorderLayout.CENTER);
	}
	
	@Override
	protected String getID() {
		return "person_profile " + person.getId();
	}
	
	@Override
	protected String getTitle() {
		return "Профиль " + person.getUsername();
	}
	
	@Override
	protected int getDockingModes() {
		return DockingMode.ALL;
	}
	
	@Override
	public ImageIcon getIconOriginal() {
		return Icons.ARTICLE_PERSON.getIcon();
	}
}
