package dev.code_offline.basalt_server.controller;

import dev.code_offline.basalt_server.model.Person;
import dev.code_offline.basalt_server.model.Role;
import dev.code_offline.basalt_server.repository.NoteRepository;
import dev.code_offline.basalt_server.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

	@GetMapping("/current")
	public ResponseEntity<Person> getCurrent(@AuthenticationPrincipal Person current) {
		return new ResponseEntity<>(current, HttpStatus.OK);
	}

	@GetMapping("/username/{username}")
	public ResponseEntity<Person> getPersonByUsername(@PathVariable String username) {
		var person = personRepository.findByUsername(username);
		
		return new ResponseEntity<>(person, HttpStatus.OK);
	}
	
	@PatchMapping("/rename")
	public ResponseEntity<Person> rename(@AuthenticationPrincipal Person currentPerson, @RequestBody String newName) {
		currentPerson.setUsername(newName);
		
		personRepository.save(currentPerson);
		sync();
		
		return new ResponseEntity<>(currentPerson, HttpStatus.OK);
	}
	
	@PatchMapping("/description")
	public ResponseEntity<Person> description(@AuthenticationPrincipal Person currentPerson, @RequestBody String newDescription) {
		currentPerson.setDescription(newDescription);
		
		personRepository.save(currentPerson);
		sync();
		
		return new ResponseEntity<>(currentPerson, HttpStatus.OK);
	}
	
	@PatchMapping("/password")
	public ResponseEntity<String> password(@AuthenticationPrincipal Person currentPerson, @RequestBody String newPassword, @RequestHeader String oldPassword) {
		if (passwordEncoder.matches(oldPassword, currentPerson.getPassword())) {
			currentPerson.setPassword(passwordEncoder.encode(newPassword));
			
			personRepository.save(currentPerson);
			
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	@Secured({"ROLE_ADMIN"})
	@PatchMapping("/{id}/role")
	public ResponseEntity<Person> role(@PathVariable Long id, @RequestBody Role role) {
		var personData = personRepository.findById(id);
		
		if (personData.isPresent()) {
			var person = personData.get();
			
			person.setRole(role);
			personRepository.save(person);
			
			sync();
			return new ResponseEntity<>(person, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@Override
	@Secured({"ROLE_ADMIN"})
	@PostMapping("/register")
	public ResponseEntity<Person> addEntity(@AuthenticationPrincipal Person currentPerson, @RequestBody Person entity) {
		return super.addEntity(currentPerson, new Person(entity.getUsername(), passwordEncoder.encode(entity.getPassword()), entity.getRole(), entity.getDescription()));
	}
	
	@Secured({"ROLE_MODERATOR"})
	@DeleteMapping("/{id}")
	public ResponseEntity<Person> deleteEntity(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestParam boolean deleteNotes) {
		if (deleteNotes) {
			noteRepository.findAll().forEach(note -> {
				if (note.getPerson() == id) {
					noteRepository.delete(note);
				}
			});
		}
		
		return super.deleteEntity(currentPerson, id);
	}
	
	@DeleteMapping("/current")
	public ResponseEntity<Person> deleteEntity(@AuthenticationPrincipal Person currentPerson, @RequestParam boolean deleteNotes) {
		if (deleteNotes) {
			noteRepository.findAll().forEach(note -> {
				if (note.getPerson() == currentPerson.getId()) {
					noteRepository.delete(note);
				}
			});
		}
		
		return super.deleteEntity(currentPerson, currentPerson.getId());
	}
	
	@Override
	@DeleteMapping
	public ResponseEntity<Person> deleteEntity(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id) {
		return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
	}
	
	@Override
	protected CrudRepository<Person, Long> getRepository() {
		return personRepository;
	}
}
