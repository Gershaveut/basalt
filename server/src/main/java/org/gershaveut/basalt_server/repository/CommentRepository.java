package org.gershaveut.basalt_server.repository;

import org.gershaveut.basalt_share.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByNote(Long note, Pageable pageable);
}
