package org.gershaveut.basalt_server.controller;

import org.gershaveut.basalt_server.repository.CommentRepository;
import org.gershaveut.basalt_server.repository.FolderRepository;
import org.gershaveut.basalt_server.repository.NoteRepository;
import org.gershaveut.basalt_server.repository.PersonRepository;
import org.gershaveut.basalt_share.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@RestController
@Secured({"ROLE_MEMBER"})
@RequestMapping("/notes")
public class NoteController extends AbstractCurdController<Note, Long> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoteController.class);

    @Autowired
    FolderRepository folderRepository;
    @Autowired
    NoteRepository noteRepository;
    @Autowired
    PersonRepository personRepository;
    @Autowired
    CommentRepository commentRepository;

    @Secured({"ROLE_GUEST"})
    @GetMapping("/{id}/text")
    public ResponseEntity<String> getText(@PathVariable Long id) {
        var noteData = noteRepository.findById(id);

        return noteData.map(t -> new ResponseEntity<>(t.getText(), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Secured({"ROLE_GUEST"})
    @GetMapping("/{id}/comments")
    public ResponseEntity<PagedModel<Comment>> getComments(@PathVariable Long id, @PageableDefault Pageable pageable) {
        if (!noteRepository.existsById(id))
            return ResponseEntity.notFound().build();
        
        var commentsPage = commentRepository.findByNote(id, pageable);
        
        return ResponseEntity.ok(new PagedModel<>(commentsPage));
    }
    
    @Secured({"ROLE_GUEST"})
    @PostMapping("/{id}/comments")
    public ResponseEntity<Comment> addComment(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody String text) {
        if (!noteRepository.existsById(id))
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(commentRepository.save(new Comment(id, currentPerson.getId(), text)));
    }
    
    @Secured({"ROLE_GUEST"})
    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Comment> removeComment(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @PathVariable Long commentId) {
        if (!noteRepository.existsById(id))
            return ResponseEntity.notFound().build();

        var commentData = commentRepository.findById(commentId);
        
        if (commentData.isEmpty() || !accessComment(currentPerson, commentData.get()))
            return ResponseEntity.notFound().build();
        
        commentRepository.deleteById(commentId);
        
        return ResponseEntity.noContent().build();
    }
    
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
    public ResponseEntity<Note> edit(@AuthenticationPrincipal Person currentPerson, @PathVariable Long id, @RequestBody(required = false) String newText) {
        if (newText == null) newText = "";

        var finalNewText = newText;
        var response = updateNote(currentPerson, id, note -> note.setText(finalNewText));
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
        if (!personRepository.existsById(newAuthor))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return updateNote(currentPerson, id, note -> note.setPerson(newAuthor));
    }

    @Secured({"ROLE_MODERATOR"})
    @PostMapping("/import")
    public ResponseEntity<List<Note>> importProject(@AuthenticationPrincipal Person currentPerson, @RequestParam("file") MultipartFile file) throws IOException {
        var zis = new ZipInputStream(file.getInputStream());
        var zipEntry = zis.getNextEntry();

        while (zipEntry != null) {
            var zipName = zipEntry.getName();

            if (zipEntry.isDirectory()) {
                folderRepository.save(new Folder(zipName.substring(0, zipName.length() - 1).replace("/", Folder.SEPARATOR)));
            } else {
                var content = new String(zis.readAllBytes());
                Note note = null;

                if (zipName.contains("/")) {
                    var lastSlashIndex = zipName.lastIndexOf('/') + 1;

                    var name = zipName.substring(lastSlashIndex);
                    var path = "@" + zipName.substring(0, lastSlashIndex - 1).replace("/", Folder.SEPARATOR);

                    if (name.endsWith(".md"))
                        note = new Note(name.split("\\.")[0], currentPerson.getId(), content, path);
                } else {
                    note = new Note(zipName.split("\\.")[0], currentPerson.getId(), content, null);
                }

                if (note != null) {
                    noteRepository.save(note);
                    updateNoteLinks(note.getId());
                }
            }

            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();

        sync();

        return getEntities();
    }

    @Secured({"ROLE_GUEST"})
    @GetMapping("/export")
    public ResponseEntity<Resource> exportProject() throws IOException {
        var fos = new ByteArrayOutputStream();
        var zipOut = new ZipOutputStream(fos);

        folderRepository.findAll().forEach(folder -> {
            try {
                var zipEntry = new ZipEntry(folder.getPath().substring(1).replace(Folder.SEPARATOR, "/") + '/');
                zipOut.putNextEntry(zipEntry);
                zipOut.closeEntry();
            } catch (Exception e) {
                LOGGER.error("Zip folder error", e);
            }
        });

        noteRepository.findAll().forEach(note -> {
            try {
                var zipEntry = new ZipEntry(note.getAbsolutePath().substring(1).replace(Folder.SEPARATOR, "/") + ".md");
                zipOut.putNextEntry(zipEntry);

                zipOut.write(note.getText().getBytes());
            } catch (Exception e) {
                LOGGER.error("Zip file error", e);
            }
        });

        zipOut.close();
        fos.close();

        var resource = new ByteArrayResource(fos.toByteArray());

        var mediaType = MediaTypeFactory
                .getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        var headers = new HttpHeaders();
        headers.setContentType(mediaType);

        var contentDisposition = ContentDisposition
                .attachment()
                .filename("export-" + new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()) + ".zip")
                .build();
        headers.setContentDisposition(contentDisposition);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
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
    
    private boolean accessComment(Person currnetPerson, Comment comment) {
        return comment.getPerson() == currnetPerson.getId() || hasRole(currnetPerson, Role.MODERATOR);
    }
    
    @Override
    protected CrudRepository<Note, Long> getRepository() {
        return noteRepository;
    }
}
