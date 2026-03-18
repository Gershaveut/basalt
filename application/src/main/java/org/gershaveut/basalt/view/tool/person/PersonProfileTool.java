package org.gershaveut.basalt.view.tool.person;

import com.javadocking.dockable.DockingMode;
import org.gershaveut.basalt.ApplicationUtil;
import org.gershaveut.basalt.view.Icons;
import org.gershaveut.basalt.view.tool.AbstractTool;
import org.gershaveut.basalt_share.model.Person;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PersonProfileTool extends AbstractTool {
	private final Person person;
	
	public PersonProfileTool(Person person) {
		this.setLayout(new BorderLayout());
		
		this.setPreferredSize(ApplicationUtil.BOX_WINDOW_DIMENSION_TOOL);
		
		this.person = person;
		
		var infoPanel = Box.createVerticalBox();
		var usernameLabel = new JLabel("Имя пользователя: " + person.getUsername());
		var roleLabel = new JLabel("Роль: " + person.getRole());
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
