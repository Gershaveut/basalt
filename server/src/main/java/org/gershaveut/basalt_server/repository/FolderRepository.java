package org.gershaveut.basalt_server.repository;

import org.gershaveut.basalt_share.model.Folder;
import org.springframework.data.repository.CrudRepository;

public interface FolderRepository extends CrudRepository<Folder, String> {
}