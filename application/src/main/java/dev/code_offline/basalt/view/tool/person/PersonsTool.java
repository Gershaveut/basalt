package dev.code_offline.basalt.view.tool.person;

import com.javadocking.dockable.DockingMode;
import dev.code_offline.basalt.ApplicationUtil;
import dev.code_offline.basalt.view.Icons;
import dev.code_offline.basalt.view.tool.AbstractTool;
import dev.code_offline.basalt_share.model.Person;
import dev.code_offline.basalt_share.model.Role;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

public class PersonsTool extends AbstractTool {
	private final EventListenerList listeners = new EventListenerList();

	private final JTree tree = new JTree(new Object[0]);
	private final JPopupMenu popupMenu = new JPopupMenu();
	
	private final JMenuItem createPerson;
	private final JMenuItem open;
	private final JMenuItem role;
	private final JMenuItem delete;
	private final JSeparator separator1;
	private final JSeparator separator2;
	
	private @Nullable TreePath selectedTreePath;
	private @Nullable Person clientPerson;
	
	public PersonsTool(JFrame parentFrame) {
		this.setLayout(new BorderLayout());
		
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	
		createPerson = new JMenuItem("Создать пользователя");
		open = new JMenuItem("Открыть профиль");
		role = new JMenuItem("Назначить роль");
		delete = new JMenuItem("Удалить");
		
		separator1 = new JSeparator();
		separator2 = new JSeparator();
		
		registerAccelerator(createPerson, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
		registerAccelerator(open, KeyStroke.getKeyStroke("ENTER"));
		registerAccelerator(role, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_DOWN_MASK));
		registerAccelerator(delete, KeyStroke.getKeyStroke("DELETE"));
		
		popupMenu.add(createPerson);
		popupMenu.add(separator1);
		popupMenu.add(open);
		popupMenu.add(separator2);
		popupMenu.add(role);
		popupMenu.add(delete);
	
		createPerson.addActionListener(e -> {
			JTextField username = new JTextField();
			JTextField password = new JPasswordField();
			JComboBox<Role> role = new JComboBox<>(Role.values());
			Object[] message = {
					"Имя пользователя:", username,
					"Пароль:", password,
					"Роль:", role
			};
			
			var option = JOptionPane.showConfirmDialog(parentFrame, message, "Создание пользователя", JOptionPane.OK_CANCEL_OPTION);
			
			if (option == JOptionPane.OK_OPTION) {
				for (PersonsListener listener : listeners.getListeners(PersonsListener.class)) {
					listener.createPerson(new Person(username.getText(), password.getText(), (Role) Objects.requireNonNull(role.getSelectedItem())));
				}
			}
		});
		open.addActionListener(e -> {
			for (PersonsListener listener : listeners.getListeners(PersonsListener.class)) {
				listener.openProfile(Objects.requireNonNull(getSelectedPerson()).getId());
			}
		});
		role.addActionListener(e -> {
			var person = Objects.requireNonNull(getSelectedPerson());
			
			JComboBox<Role> role = new JComboBox<>(Role.values());
			Object[] message = {
				"Роль:", role
			};
			
			var option = JOptionPane.showConfirmDialog(parentFrame, message, "Выбор роли " + person.getUsername(), JOptionPane.OK_CANCEL_OPTION);
			
			if (option == JOptionPane.OK_OPTION) {
				for (PersonsListener listener : listeners.getListeners(PersonsListener.class)) {
					listener.rolePerson(person.getId(), (Role) Objects.requireNonNull(role.getSelectedItem()));
				}
			}
		});
		delete.addActionListener(e -> {
			var confirm = JOptionPane.showConfirmDialog(parentFrame, "Удалить все записки созданные эти пользователем?", "Удаление " + Objects.requireNonNull(getSelectedPerson()).getUsername(), JOptionPane.YES_NO_CANCEL_OPTION);
			
			if (confirm != JOptionPane.CANCEL_OPTION) {
				for (PersonsListener listener : listeners.getListeners(PersonsListener.class)) {
					listener.deletePerson(Objects.requireNonNull(getSelectedPerson()).getId(), confirm == JOptionPane.YES_OPTION);
				}
			}
		});
		
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					int selRow = tree.getRowForLocation(e.getX(), e.getY());
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
					
					if (selPath != null) {
						tree.setSelectionPath(selPath);
					}
					
					if (selRow > -1) {
						tree.setSelectionRow(selRow);
					}
					
					showPopupMenu(e.getX(), e.getY());
				}
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (getSelectedPerson() != null && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
					open.doClick();
				}
			}
		});
		tree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (ApplicationUtil.isContextKey(e))
					showPopupMenu(0, 0);
			}
		});
		tree.addTreeSelectionListener(e -> {
			selectedTreePath = e.getNewLeadSelectionPath();
		});
		
		add(new JScrollPane(tree), BorderLayout.CENTER);
	}
	
	private @Nullable Person getSelectedPerson() {
		if (selectedTreePath == null)
			return null;
		
		var object = ((DefaultMutableTreeNode) selectedTreePath.getLastPathComponent()).getUserObject();
		
		if (object instanceof Person person) {
			return person;
		} else {
			return null;
		}
	}
	
	private void showPopupMenu(int x, int y) {
		updateMenuContext();
		
		if (ApplicationUtil.anyComponentsVisible(popupMenu))
			popupMenu.show(tree, x, y);
	}
	
	private void updateMenuContext() {
		var hasAdmin = ApplicationUtil.hasRole(clientPerson, Role.ADMIN);
		
		createPerson.setVisible(false);
		separator1.setVisible(false);
		open.setVisible(false);
		separator2.setVisible(false);
		role.setVisible(false);
		delete.setVisible(false);
		
		if (hasAdmin)
			createPerson.setVisible(true);
		
		if (getSelectedPerson() != null) {
			if (hasAdmin)
				separator1.setVisible(true);
			
			open.setVisible(true);
			
			if (ApplicationUtil.hasRole(clientPerson, Role.MODERATOR)) {
				separator2.setVisible(true);
				role.setVisible(true);
				delete.setVisible(true);
			}
		}
	}
	
	public void addPersonsListener(PersonsListener personsListener) {
		listeners.add(PersonsListener.class, personsListener);
	}
	
	public void removeListener(PersonsListener personsListener) {
		listeners.remove(PersonsListener.class, personsListener);
	}
	
	public String getExpansionState() {
		var string = new StringBuilder();
		
		for (int i = 0; i < tree.getRowCount(); i++) {
			TreePath treePath = tree.getPathForRow(i);
			
			if (tree.isExpanded(i)) {
				string.append(treePath.toString());
				string.append(",");
			}
		}
		
		return string.toString();
	}
	
	public void setExpansionState(String state) {
		for (int i = 0; i < tree.getRowCount(); i++) {
			TreePath treePath = tree.getPathForRow(i);
			
			if (state.contains(treePath.toString())) {
				tree.expandRow(i);
			}
		}
	}
	
	public void setModel(List<Person> persons, Person clientPerson) {
		this.clientPerson = clientPerson;
		
		var rootNode = new DefaultMutableTreeNode();
		
		var guestNode = new DefaultMutableTreeNode(Role.GUEST);
		var memberNode = new DefaultMutableTreeNode(Role.MEMBER);
		var moderatorNode = new DefaultMutableTreeNode(Role.MODERATOR);
		var adminNode = new DefaultMutableTreeNode(Role.ADMIN);
		
		var listRoles = List.of(guestNode, memberNode, moderatorNode, adminNode);
		
		persons.forEach(person -> {
			listRoles.stream().filter(treeNode -> treeNode.getUserObject() == person.getRole()).findFirst().orElseThrow()
					.add(new DefaultMutableTreeNode(person));
		});
		
		listRoles.forEach(rootNode::add);
		
		var state = getExpansionState();
		
		tree.setModel(new JTree(rootNode).getModel());
		
		setExpansionState(state);
	}
	
	private void registerAccelerator(JMenuItem menuItem, KeyStroke keyStroke) {
		ApplicationUtil.registerAccelerator(menuItem, tree, keyStroke, this::updateMenuContext);
	}
	
	public JTree getTree() {
		return tree;
	}
	
	@Override
	protected String getID() {
		return "persons";
	}
	
	@Override
	protected String getTitle() {
		return "Пользователи";
	}
	
	@Override
	protected int getDockingModes() {
		return DockingMode.ALL;
	}
	
	@Override
	public @Nullable ImageIcon getIconOriginal() {
		return Icons.PERSON.getIcon();
	}
}
