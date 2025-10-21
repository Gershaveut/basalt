package dev.code_offline.basalt_server.repository;

import dev.code_offline.basalt_server.model.Folder;
import org.springframework.data.repository.CrudRepository;

public interface FolderRepository extends CrudRepository<Folder, String> {
}