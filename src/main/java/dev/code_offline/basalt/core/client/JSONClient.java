package dev.code_offline.basalt.core.client;

import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import dev.code_offline.basalt.Main;
import dev.code_offline.basalt.model.Folder;
import dev.code_offline.basalt.model.JSONDatabaseModel;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.person.Person;
import dev.code_offline.basalt.model.person.Role;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.naming.SizeLimitExceededException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

public class JSONClient extends Client {
    private final String FILE_NAME = "database.json";

    private JSONDatabaseModel databaseModel = new JSONDatabaseModel();

    private final Person clientPerson = new Person("Вы", 0, Role.ADMIN, null);

    public JSONClient() {
        super();

        try {
            var ignored = new File(FILE_NAME).createNewFile();
            var path = Path.of(FILE_NAME);
            
            if (Files.size(path) > Integer.MAX_VALUE >> 1) {
                throw new SizeLimitExceededException("Json database too big");
            }
            
            @Nullable JSONDatabaseModel database = new Gson().fromJson(Files.readString(path), JSONDatabaseModel.class);

            if (database != null)
                this.databaseModel = database;
        } catch (Exception e) {
            Main.logger.severe("Error load json database: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isOffline() {
        return true;
    }
    
    private void save() {
        Main.logger.info("Saving database...");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME));
            writer.write(new Gson().newBuilder().setFormattingStyle(FormattingStyle.PRETTY).create().toJson(databaseModel));

            writer.close();

            notifyListeners();
        } catch (IOException e) {
            Main.logger.severe("Error save json database: " + e.getMessage());
        }
    }

    private int getNextId(IntStream intStream) {
        return intStream.max().orElse(0) + 1;
    }

    @Override
    public Person getClientPerson() {
        return clientPerson;
    }

    @Override
    public List<Person> getPersons() {
        return databaseModel.getPersons();
    }

    @Override
    public List<Note> getNotes() {
        return databaseModel.getNotes();
    }

    @Override
    public List<Folder> getFolders() {
        return databaseModel.getFolders();
    }

    @Override
    public Folder getRoot() {
        return databaseModel.getRoot();
    }

    @Override
    public Note getNote(long id) {
        return databaseModel.getNotes().stream().filter(n -> n.getId() == id).findFirst().orElseThrow();
    }

    @Override
    public Person getPerson(long id) {
        return databaseModel.getPersons().stream().filter(p -> p.getId() == id).findFirst().orElseThrow();
    }
    
    @Override
    public void addPerson(Person person) {
        person.setId(getNextId(databaseModel.getPersons().stream().mapToInt(p -> Math.toIntExact(p.getId()))));

        databaseModel.getPersons().add(person);
        save();
    }

    @Override
    public void addNote(Note note) {
        note.setId(getNextId(databaseModel.getNotes().stream().mapToInt(n -> Math.toIntExact(n.getId()))));

        databaseModel.getNotes().add(note);
        save();
    }

    @Override
    public void addFolder(Folder folder) {
        databaseModel.getFolders().add(folder);
        save();
    }

    @Override
    public void renameFolder(String path, String newName) {
        var targetFolder = databaseModel.getFolders().stream().filter(f -> f.getPath().equals(path)).findFirst().orElseThrow();
        targetFolder.setName(newName);
        
        databaseModel.getNotes().stream().filter(n -> n.getParent().getPath().equals(path)).forEach(note -> {
            note.setParent(targetFolder);
        });
        
        databaseModel.getFolders().stream().filter(f -> {
            assert f.getParent() != null;
            return f.getParent().getPath().equals(path);
        }).forEach(folder1 -> {
            moveFolderWork(folder1.getPath(), targetFolder);
        });
        
        save();
    }
    
    @Override
    public void moveFolder(String path, Folder folder) {
        moveFolderWork(path, folder);
        
        save();
    }
    
    private void moveFolderWork(String path, Folder folder) {
        var targetFolder = databaseModel.getFolders().stream().filter(f -> f.getPath().equals(path)).findFirst().orElseThrow();
        targetFolder.setParent(folder);
        
        databaseModel.getNotes().stream().filter(n -> n.getParent().getPath().equals(path)).forEach(note -> {
            note.setParent(targetFolder);
        });
        
        databaseModel.getFolders().stream().filter(f -> {
			assert f.getParent() != null;
			return f.getParent().getPath().equals(path);
		}).forEach(folder1 -> {
            moveFolderWork(folder1.getPath(), targetFolder);
        });
    }
    
    @Override
    public void deleteFolder(String path) {
        deleteFolderWork(databaseModel.getFolders().stream().filter(f -> f.getPath().equals(path)).findFirst().orElseThrow());
        
        save();
    }
    
    private void deleteFolderWork(Folder folder) {
        databaseModel.getFolders().remove(folder);
        
        var findNotes = databaseModel.getNotes().stream().filter(n -> n.getParent().getPath().equals(folder.getPath())).toList();
        
        findNotes.forEach(note -> databaseModel.getNotes().remove(note));
        
        var findFolders = databaseModel.getFolders().stream().filter(f -> {
            assert f.getParent() != null;
            return f.getParent().getPath().equals(folder.getPath());
        }).toList();
        
        findFolders.forEach(this::deleteFolderWork);
    }
    
    @Override
    public void editNote(long id, String newText) {
        databaseModel.getNotes().stream().filter(note -> note.getId() == id).findFirst().orElseThrow().setText(newText);
        save();
    }

    @Override
    public void renameNote(long id, String newName) {
        databaseModel.getNotes().stream().filter(note -> note.getId() == id).findFirst().orElseThrow().setName(newName);
        save();
    }
    
    @Override
    public void moveNote(long id, Folder folder) {
        databaseModel.getNotes().stream().filter(note -> note.getId() == id).findFirst().orElseThrow().setParent(folder);
        save();
    }
    
    @Override
    public void deleteNote(long id) {
        databaseModel.getNotes().removeIf(n -> n.getId() == id);
        save();
    }
}
