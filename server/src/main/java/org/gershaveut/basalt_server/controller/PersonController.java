package org.gershaveut.basalt_server.controller;

import org.gershaveut.basalt_server.repository.FileRepository;
import org.gershaveut.basalt_server.repository.PersonRepository;
import org.gershaveut.basalt_share.model.Person;
import org.gershaveut.basalt_share.model.Role;
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
	FileRepository fileRepository;
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
		if (personRepository.findByUsername(newName) != null)
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		
		var person = personRepository.findById(currentPerson.getId()).orElseThrow();
		
		person.setUsername(newName);
		
		personRepository.save(person);
		sync();
		
		return new ResponseEntity<>(person, HttpStatus.OK);
	}
	
	@PatchMapping("/description")
	public ResponseEntity<Person> description(@AuthenticationPrincipal Person currentPerson, @RequestBody String newDescription) {
		String description = null;
		
		if (!newDescription.isEmpty())
			description = newDescription;
		
		var person = personRepository.findById(currentPerson.getId()).orElseThrow();
		
		person.setDescription(description);
		
		personRepository.save(person);
		sync();
		
		return new ResponseEntity<>(person, HttpStatus.OK);
	}
	
	@PatchMapping("/password")
	public ResponseEntity<String> password(@AuthenticationPrincipal Person currentPerson, @RequestBody String newPassword, @RequestHeader String oldPassword) {
		if (passwordEncoder.matches(oldPassword, currentPerson.getPassword())) {
			var person = personRepository.findById(currentPerson.getId()).orElseThrow();
			
			person.setPassword(passwordEncoder.encode(newPassword));
			
			personRepository.save(person);
			
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
		if (personRepository.findByUsername(entity.getUsername()) == null) {
			return super.addEntity(currentPerson, new Person(entity.getUsername(), passwordEncoder.encode(entity.getPassword()), entity.getRole(), entity.getDescription()));
		} else {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
	}
	
	@Secured({"ROLE_MODERATOR"})
	@DeleteMapping("/{id}")
	public ResponseEntity<Person> deleteEntity(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestParam boolean deleteNotes) {
		if (deleteNotes) {
			fileRepository.findAll().forEach(note -> {
				if (note.getPerson() == id) {
					fileRepository.delete(note);
				}
			});
		}
		
		return super.deleteEntity(currentPerson, id);
	}
	
	@DeleteMapping("/current")
	public ResponseEntity<Person> deleteEntity(@AuthenticationPrincipal Person currentPerson, @RequestParam boolean deleteNotes) {
		if (deleteNotes) {
			fileRepository.findAll().forEach(note -> {
				if (note.getPerson() == currentPerson.getId()) {
					fileRepository.delete(note);
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
