package dev.code_offline.basalt_server.controller;

import dev.code_offline.basalt_server.model.Folder;
import dev.code_offline.basalt_server.repository.FolderRepository;
import dev.code_offline.basalt_server.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.function.Consumer;

@RestController
@RequestMapping("/folders")
public class FolderController extends AbstractCurdController<Folder, String> {
	@Autowired
	FolderRepository folderRepository;
	@Autowired
	NoteRepository noteRepository;

	@PatchMapping("/{id}/move")
	public ResponseEntity<Folder> move(@PathVariable String id, @RequestBody String path) {
		if (id.equals(path))
			return new ResponseEntity<>(null, HttpStatus.CONFLICT);
		
		var response = updateFolder(id, folder -> folder.setParent(new Folder(path)));
		
		sync();
		return response;
	}
	
	@PatchMapping("/{id}/rename")
	public ResponseEntity<Folder> rename(@PathVariable String id, @RequestBody String newName) {
		var response = updateFolder(id, folder -> folder.setName(newName));
		sync();
		return response;
	}
	
	@Override
	public ResponseEntity<Folder> deleteEntity(@PathVariable String id) {
		noteRepository.findAll().forEach(note -> {
			if (Objects.equals(note.getPath(), id)) {
				noteRepository.delete(note);
			}
		});
		
		folderRepository.findAll().forEach(folder -> {
			if (folder.getParent() != null && Objects.equals(folder.getParent().getPath(), id))
				deleteEntity(id);
		});
		
		return super.deleteEntity(id);
	}
	
	private ResponseEntity<Folder> updateFolder(String id, Consumer<Folder> updateAction) {
		var targetData = folderRepository.findById(id);
		
		if (targetData.isEmpty())
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		
		var target = targetData.get();
		
		folderRepository.delete(target);
		updateAction.accept(target);
		folderRepository.save(target);
		
		noteRepository.findAll().forEach(note -> {
			if (note.getPath().equals(target.getPath())) {
				note.setPath(target.getPath());
				noteRepository.save(note);
			}
		});
		
		folderRepository.findAll().forEach(folder -> {
			if (folder.getParent() != null && folder.getParent().getPath().equals(id)) {
				updateFolder(folder.getPath(), f -> f.setParent(target));
			}
		});
		
		return new ResponseEntity<>(target, HttpStatus.OK);
	}
	
	@Override
	protected CrudRepository<Folder, String> getRepository() {
		return folderRepository;
	}
}
