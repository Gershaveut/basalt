package dev.code_offline.basalt.view.tool.person;

import dev.code_offline.basalt.model.person.Person;
import dev.code_offline.basalt.model.person.Role;

import java.util.EventListener;

public interface PersonsListener extends EventListener {
	void createPerson(Person person);
	void openProfile(long id);
	void rolePerson(long id, Role role);
	void deletePerson(long id, boolean deleteNotes);
}
