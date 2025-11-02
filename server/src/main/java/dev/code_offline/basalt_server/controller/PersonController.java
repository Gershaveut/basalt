package dev.code_offline.basalt_server.controller;

import dev.code_offline.basalt_server.model.Person;
import dev.code_offline.basalt_server.model.Role;
import dev.code_offline.basalt_server.repository.NoteRepository;
import dev.code_offline.basalt_server.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/persons")
public class PersonController extends AbstractCurdController<Person, Long> {
	@Autowired
	PersonRepository personRepository;
	@Autowired
	NoteRepository noteRepository;
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Override
	@PostMapping("/register")
	public ResponseEntity<Person> addEntity(@RequestBody Person entity) {
		return super.addEntity(new Person(entity.getUsername(), passwordEncoder.encode(entity.getPassword()), entity.getRole(), entity.getDescription()));
	}
	
	@Override
	public ResponseEntity<Person> deleteEntity(@PathVariable Long id) {
		noteRepository.findAll().forEach(note -> {
			if (note.getPerson() == id) {
				noteRepository.delete(note);
			}
		});
		
		return super.deleteEntity(id);
	}
	
	@Override
	protected CrudRepository<Person, Long> getRepository() {
		return personRepository;
	}
}
