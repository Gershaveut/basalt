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

            @Nullable JSONDatabaseModel database = new Gson().fromJson(Files.readString(Path.of(FILE_NAME)), JSONDatabaseModel.class);

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
        databaseModel.getFolders().stream().filter(f -> f.getPath().equals(path)).findFirst().orElseThrow().setName(newName);
        save();
    }

    @Override
    public void deleteFolder(String path) {
        databaseModel.getFolders().removeIf(f -> f.getPath().equals(path));
        save();
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
    public void deleteNote(long id) {
        databaseModel.getNotes().removeIf(n -> n.getId() == id);
        save();
    }
}
