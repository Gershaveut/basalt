package org.gershaveut.basalt_server.controller;

import org.gershaveut.basalt_server.model.File;
import org.gershaveut.basalt_server.repository.FileRepository;
import org.gershaveut.basalt_share.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

@RestController
@RequestMapping("/folders")
public class FolderController extends AbstractFileController {
	@Autowired
	FileRepository fileRepository;

	@PatchMapping("/{id}/move")
	public ResponseEntity<File> move(@PathVariable String id, @RequestBody String path) {
		var newFolder = new File();
		newFolder.setParent(path);
		
		if (id.equals(path) || fileRepository.existsById(newFolder.getPath()))
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		
		var response = updateFolder(id, folder -> folder.setPath(path));
		
		sync();
		return response;
	}
	
	@PatchMapping("/{id}/rename")
	public ResponseEntity<Folder> rename(@PathVariable String id, @RequestBody String newName) {
		var newFolder = Folder.of(id);
		newFolder.setName(newName);
		
		if (folderRepository.existsById(newFolder.getPath()))
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		
		var response = updateFolder(id, folder -> folder.setName(newName));
		sync();
		return response;
	}
	
	@Override
	public ResponseEntity<Folder> deleteEntity(@AuthenticationPrincipal Person currentPerson, @PathVariable String id) {
		Queue<String> queue = new LinkedList<>();
		queue.add(id);
		
		while (!queue.isEmpty()) {
			var currentId = queue.poll();
			
			fileRepository.findAll().forEach(note -> {
				if (note.getPath() != null && note.getPath().equals(currentId)) {
					fileRepository.delete(note);
				}
			});
			
			folderRepository.deleteById(currentId);
			
			folderRepository.findAll().forEach(folder -> {
				if (folder.getParent() != null && folder.getParent().getPath().equals(currentId))
					queue.add(folder.getPath());
			});
		}
		
		sync();
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	private ResponseEntity<File> updateFolder(Long id, Consumer<File> updateAction) {
		var targetData = fileRepository.findById(id);
		
		if (targetData.isEmpty())
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		
		var target = targetData.get();
		
		fileRepository.delete(target);
		updateAction.accept(target);
		fileRepository.save(target);
		
		fileRepository.findAll().forEach(note -> {
            if (note.getPath() != null) {
                note.getPath();
            }
        });
		
		fileRepository.findAll().forEach(folder -> {
            if (folder.getParent() != null) {
                folder.getParent();
            }
        });
		
		return new ResponseEntity<>(target, HttpStatus.OK);
	}
}
