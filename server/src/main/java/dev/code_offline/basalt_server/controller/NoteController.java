package dev.code_offline.basalt_server.controller;

import dev.code_offline.basalt_server.model.Note;
import dev.code_offline.basalt_server.model.Person;
import dev.code_offline.basalt_server.model.Role;
import dev.code_offline.basalt_server.repository.NoteRepository;
import dev.code_offline.basalt_server.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.function.Consumer;

@RestController
@Secured({"ROLE_MEMBER"})
@RequestMapping("/notes")
public class NoteController extends AbstractCurdController<Note, Long> {
	@Autowired
	NoteRepository noteRepository;
	@Autowired
	PersonRepository personRepository;
	
	@Override
	public ResponseEntity<Note> addEntity(@AuthenticationPrincipal Person currentPerson, @RequestBody Note entity) {
		return super.addEntity(currentPerson, new Note(entity.getName(), currentPerson.getId(), entity.getText(), entity.getPath()));
	}
	
	@PatchMapping("/{id}/rename")
	public ResponseEntity<Note> rename(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody String newName) {
		return updateNote(currentPerson, id, note -> note.setName(newName));
	}
	
	@PatchMapping("/{id}/edit")
	public ResponseEntity<Note> edit(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody String newText) {
		return updateNote(currentPerson, id, note -> note.setText(newText));
	}
	
	@PatchMapping("/{id}/move")
	public ResponseEntity<Note> move(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody String newPath) {
		return updateNote(currentPerson, id, note -> note.setPath(newPath));
	}
	
	@Secured({"ROLE_MODERATOR"})
	@PatchMapping("/{id}/author")
	public ResponseEntity<Note> author(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody Long newAuthor) {
		return updateNote(currentPerson, id, note -> note.setPerson(newAuthor));
	}
	
	private ResponseEntity<Note> updateNote(Person currnetPerson, Long id, Consumer<Note> updateAction) {
		var noteData = noteRepository.findById(id);
		
		if (noteData.isPresent()) {
			var note = noteData.get();
			
			if (note.getPerson() == currnetPerson.getId() || hasRole(currnetPerson, Role.MODERATOR)) {
				updateAction.accept(note);
				noteRepository.save(note);
				
				sync();
				return new ResponseEntity<>(note, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@Override
	protected CrudRepository<Note, Long> getRepository() {
		return noteRepository;
	}
}
