package org.gershaveut.basalt_server.repository;

import org.gershaveut.basalt_share.Util;
import org.gershaveut.basalt_server.model.SFile;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface FileRepository extends CrudRepository<SFile, Long> {
	Optional<SFile> findByNameAndPath(String name, String path);
	
	default Optional<SFile> findByAbsolutePath(String absolutePath) {
		var splitAbsolutePath = Util.splitAbsolutePath(absolutePath, SFile.SEPARATOR);
	
		var path = splitAbsolutePath.getSecond();
		
		if (splitAbsolutePath.getSecond().equals("@")) {
			path = null;
		}
		
		return findByNameAndPath(splitAbsolutePath.getFirst(), path);
	}
}