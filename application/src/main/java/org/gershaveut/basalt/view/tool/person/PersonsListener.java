package org.gershaveut.basalt.view.tool.person;

import org.gershaveut.basalt_share.model.Person;
import org.gershaveut.basalt_share.model.Role;

import java.util.EventListener;

public interface PersonsListener extends EventListener {
	void createPerson(Person person);
	void openProfile(long id);
	void rolePerson(long id, Role role);
	void deletePerson(long id, boolean deleteNotes);
}
