package dev.code_offline.basalt_server.controller;

import dev.code_offline.basalt_share.model.Note;
import dev.code_offline.basalt_share.model.Person;
import dev.code_offline.basalt_share.model.Role;
import dev.code_offline.basalt_server.repository.NoteRepository;
import dev.code_offline.basalt_server.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.EntityResponse;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

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
		var name = entity.getName();
		var number = 0;
		
		while (noteRepository.findByName(name) != null) {
			++number;
			name = entity.getName() + " " + number;
		}
		
		var response = super.addEntity(currentPerson, new Note(name, currentPerson.getId(), entity.getText(), entity.getPath()));
		var body = response.getBody();
		
		if (body != null)
			return updateNoteLinks(body.getId());
		
		return response;
	}

	@Override
	@Secured({"ROLE_MEMBER"})
	public ResponseEntity<Note> deleteEntity(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id) {
		var noteData = noteRepository.findById(id);
		
		if (noteData.isPresent()) {
			if (accessNote(currentPerson, noteData.get())) {
				return super.deleteEntity(currentPerson, id);
			} else {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@PatchMapping("/{id}/rename")
	public ResponseEntity<Note> rename(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody String newName) {
		if (noteRepository.findByName(newName) != null)
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		
		var response = updateNote(currentPerson, id, note -> note.setName(newName));
		var body = response.getBody();
		
		if (body != null) {
			noteRepository.findAll().forEach(note -> updateNoteLinks(note.getId()));
		}
		
		return response;
	}
	
	@PatchMapping("/{id}/edit")
	public ResponseEntity<Note> edit(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody String newText) {
		var response = updateNote(currentPerson, id, note -> note.setText(newText));
		var body = response.getBody();
		
		if (body != null)
			return updateNoteLinks(body.getId());
		
		return response;
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
			
			if (accessNote(currnetPerson, note)) {
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
	
	private ResponseEntity<Note> updateNoteLinks(Long id) {
		var noteData = noteRepository.findById(id);
	
		if (noteData.isPresent()) {
			var note = noteData.get();
			
			var links = new ArrayList<Long>();
			
			var patternId = Pattern.compile("\\{(\\d*?)}");
			var patternName = Pattern.compile("\\[\\[(.*?)]]");
			
			var matcherId = patternId.matcher(note.getText());
			var matcherName = patternName.matcher(note.getText());
			
			while (matcherId.find()) {
				try {
					var number = Long.parseLong(matcherId.group(1).trim());
					
					if (number != note.getId() && links.stream().noneMatch(l -> l == number))
						links.add(number);
				} catch (Exception ignored) {
				}
			}
			
			while (matcherName.find()) {
				try {
					var name = matcherName.group(1).trim();
					
					var number = noteRepository.findByName(name).getId();
					
					if (number != note.getId() && links.stream().noneMatch(l -> l == number))
						links.add(number);
				} catch (Exception ignored) {
				}
			}
			
			note.setLinks(links);
			noteRepository.save(note);
			
			return new ResponseEntity<>(note, HttpStatus.OK);
		}
		
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	private boolean accessNote(Person currnetPerson, Note note) {
		return hasRole(currnetPerson, Role.MEMBER) && note.getPerson() == currnetPerson.getId() || hasRole(currnetPerson, Role.MODERATOR);
	}
	
	@Override
	protected CrudRepository<Note, Long> getRepository() {
		return noteRepository;
	}
}
