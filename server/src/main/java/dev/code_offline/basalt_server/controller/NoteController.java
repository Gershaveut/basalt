package dev.code_offline.basalt_server.controller;

import dev.code_offline.basalt_server.model.Note;
import dev.code_offline.basalt_server.repository.NoteRepository;
import dev.code_offline.basalt_server.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notes")
public class NoteController extends AbstractCurdController<Note, Long> {
	@Autowired
	NoteRepository noteRepository;
	@Autowired
	PersonRepository personRepository;
	
	@Override
	public ResponseEntity<Note> addEntity(@RequestBody Note entity) {
		var auth = SecurityContextHolder.getContext().getAuthentication();
		
		return super.addEntity(new Note(entity.getName(), personRepository.findByUsername(auth.getName()).getId(), entity.getText(), entity.getPath()));
	}
	
	@PatchMapping("/{id}/rename")
	public ResponseEntity<Note> rename(@PathVariable Long id, @RequestBody String newName) {
			var noteData = noteRepository.findById(id);
			
			if (noteData.isPresent()) {
				var note = noteData.get();
				
				note.setName(newName);
				noteRepository.save(note);
				
				sync();
				return new ResponseEntity<>(note, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
	}
	
	@PatchMapping("/{id}/edit")
	public ResponseEntity<Note> edit(@PathVariable Long id, @RequestBody String newText) {
		var noteData = noteRepository.findById(id);
		
		if (noteData.isPresent()) {
			var note = noteData.get();
			
			note.setText(newText);
			noteRepository.save(note);
			
			sync();
			return new ResponseEntity<>(note, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@PatchMapping("/{id}/move")
	public ResponseEntity<Note> move(@PathVariable Long id, @RequestBody String path) {
		var noteData = noteRepository.findById(id);
		
		if (noteData.isPresent()) {
			var note = noteData.get();
			
			note.setPath(path);
			noteRepository.save(note);
			
			sync();
			return new ResponseEntity<>(note, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@Override
	protected CrudRepository<Note, Long> getRepository() {
		return noteRepository;
	}
}
