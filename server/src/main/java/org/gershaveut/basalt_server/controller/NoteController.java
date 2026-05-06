package org.gershaveut.basalt_server.controller;

import org.gershaveut.basalt_server.model.File;
import org.gershaveut.basalt_server.model.Note;
import org.gershaveut.basalt_server.repository.CommentRepository;
import org.gershaveut.basalt_server.repository.FileRepository;
import org.gershaveut.basalt_share.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@Secured({"ROLE_MEMBER"})
@RequestMapping("/notes")
public class NoteController extends AbstractFileController {
    @Autowired
    FileRepository fileRepository;
    @Autowired
    CommentRepository commentRepository;

    @Override
    public ResponseEntity<List<File>> getEntities() {
        var entities = new ArrayList<File>();

        fileRepository.findAllNotes().forEach(entities::add);

        if (entities.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(entities, HttpStatus.OK);
    }
    
    @Override
    public ResponseEntity<File> getEntity(@PathVariable Long id) {
        var noteData = fileRepository.findNoteById(id);
        
        if (noteData.isPresent())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return fileRepository.findById(id).map(t -> new ResponseEntity<>(t, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @Override
    public ResponseEntity<File> addEntity(@AuthenticationPrincipal Person currentPerson, @RequestBody File entity) {
        var response = super.addEntity(currentPerson, new Note(entity.getName(), currentPerson.getId(), entity.getContent(), entity.getPath()));
        var body = response.getBody();

        if (body != null)
            return updateNoteLinks(body.getId());

        return response;
    }

    @Override
    public ResponseEntity<File> rename(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody String newName) {
        var response = super.rename(currentPerson, id, newName);
        var body = response.getBody();

        if (body != null) {
            fileRepository.findAllNotes().forEach(note -> updateNoteLinks(note.getId()));
        }

        return response;
    }

    @Override
    public ResponseEntity<File> edit(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody(required = false) String newContent) {
        var response = super.edit(currentPerson, id, newContent); 
        var body = response.getBody();

        if (body != null)
            return updateNoteLinks(body.getId());

        return response;
    }

    @Override
    public ResponseEntity<List<File>> importProject(@AuthenticationPrincipal Person currentPerson, @RequestParam("file") MultipartFile file) throws IOException {
        var response = super.importProject(currentPerson, file);
        var body = response.getBody();

        if (body != null) {
            fileRepository.findAllNotes().forEach(note -> updateNoteLinks(note.getId()));
        }
        
        return response;
    }

    @Secured({"ROLE_GUEST"})
    @GetMapping("/{id}/text")
    public ResponseEntity<String> getText(@PathVariable Long id) {
        var noteData = fileRepository.findNoteById(id);

        return noteData.map(t -> new ResponseEntity<>(t.getText(), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Secured({"ROLE_GUEST"})
    @GetMapping("/{id}/comments")
    public ResponseEntity<PagedModel<Comment>> getComments(@PathVariable Long id, @PageableDefault Pageable pageable) {
        if (!fileRepository.existsById(id))
            return ResponseEntity.notFound().build();

        var commentsPage = commentRepository.findByNote(id, pageable);

        return ResponseEntity.ok(new PagedModel<>(commentsPage));
    }

    @Secured({"ROLE_GUEST"})
    @PostMapping("/{id}/comments")
    public ResponseEntity<Comment> addComment(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody String text) {
        if (!fileRepository.existsById(id))
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(commentRepository.save(new Comment(id, currentPerson.getId(), text)));
    }

    @Secured({"ROLE_GUEST"})
    @PostMapping("/{id}/comments/{commentId}/edit")
    public ResponseEntity<Comment> editComment(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @PathVariable Long commentId, @RequestBody String text) {
        if (!fileRepository.existsById(id))
            return ResponseEntity.notFound().build();

        var commentData = commentRepository.findById(commentId);

        if (commentData.isEmpty() || !accessComment(currentPerson, commentData.get()))
            return ResponseEntity.notFound().build();

        var comment = commentData.get();

        comment.setText(text);
        comment.setEdited(true);

        return ResponseEntity.ok(commentRepository.save(comment));
    }

    @Secured({"ROLE_GUEST"})
    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Comment> deleteComment(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @PathVariable Long commentId) {
        if (!fileRepository.existsById(id))
            return ResponseEntity.notFound().build();

        var commentData = commentRepository.findById(commentId);

        if (commentData.isEmpty() || !accessComment(currentPerson, commentData.get()))
            return ResponseEntity.notFound().build();

        commentRepository.deleteById(commentId);

        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<File> updateNoteLinks(Long id) {
        var noteData = fileRepository.findNoteById(id);

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

                    var number = fileRepository.findByAbsolutePath(name).orElseThrow().getId();

                    if (number != note.getId() && links.stream().noneMatch(l -> l == number))
                        links.add(number);
                } catch (Exception ignored) {
                }
            }

            note.setLinks(links);
            fileRepository.save(note);

            return new ResponseEntity<>(note, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private boolean accessComment(Person currnetPerson, Comment comment) {
        return comment.getPerson() == currnetPerson.getId() || hasRole(currnetPerson, Role.MODERATOR);
    }
}
