package dev.code_offline.basalt.view.tool.persons;

import dev.code_offline.basalt.model.person.Person;

import java.util.EventListener;

public interface PersonsListener extends EventListener {
	void createPerson(Person person);
	void openProfile(long id);
	void deletePerson(long id, boolean deleteNotes);
}
