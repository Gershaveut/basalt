package org.gershaveut.basalt_server.repository;

import org.gershaveut.basalt_share.Util;
import org.gershaveut.basalt_server.model.SFile;
import org.gershaveut.basalt_share.model.Folder;
import org.gershaveut.basalt_share.model.Image;
import org.gershaveut.basalt_server.model.Note;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.stream.StreamSupport;


public interface FileRepository extends CrudRepository<SFile, Long> {
	Optional<SFile> findByNameAndPath(String name, String path);
	
	default Optional<SFile> findByAbsolutePath(String absolutePath) {
		var splitAbsolutePath = Util.splitAbsolutePath(absolutePath, Folder.SEPARATOR);
	
		var path = splitAbsolutePath.getSecond();
		
		if (splitAbsolutePath.getSecond().equals("@")) {
			path = null;
		}
		
		return findByNameAndPath(splitAbsolutePath.getFirst(), path);
	}

	default Optional<Note> findNoteById(Long id) {
		return findById(id)
				.filter(Note.class::isInstance)
				.map(Note.class::cast);
	}

	default Optional<Image> findImageById(Long id) {
		return findById(id)
				.filter(Image.class::isInstance)
				.map(Image.class::cast);
	}

	default Iterable<Note> findAllNotes() {
		return () -> StreamSupport.stream(findAll().spliterator(), false)
				.filter(Note.class::isInstance)
				.map(Note.class::cast)
				.iterator();
	}
	
	default Iterable<Image> findAllImages() {
		return () -> StreamSupport.stream(findAll().spliterator(), false)
				.filter(Image.class::isInstance)
				.map(Image.class::cast)
				.iterator();
	}
}