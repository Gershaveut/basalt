package dev.code_offline.basalt.model.client.json;

import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import dev.code_offline.basalt.Main;
import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.client.Client;
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

public class JSONClient extends Client {
    private final String FILE_NAME = "database.json";

    private JSONDatabaseModel databaseModel;

    private final Person clientPerson = new Person("Вы", 0, Role.ADMIN, null);

    public JSONClient() {
        super();

        try {
            var ignored = new File(FILE_NAME).createNewFile();

            @Nullable JSONDatabaseModel database = new Gson().fromJson(Files.readString(Path.of(FILE_NAME)), JSONDatabaseModel.class);

            if (database == null) {
                database = new JSONDatabaseModel();
            }

            this.databaseModel = database;
        } catch (Exception e) {
            Main.logger.severe("Error load json database: " + e.getMessage());
            throw new RuntimeException(e);
        }
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

    @Override
    public List<Person> getPersons() {
        return databaseModel.getPersons();
    }

    @Override
    public List<Note> getNotes() {
        return databaseModel.getNotes();
    }

    @Override
    public void addPerson(Person person) {
        databaseModel.getPersons().add(person);
        save();
    }

    @Override
    public void addNote(Note note) {
        if (note.getId() == -1) {
            var maxId = databaseModel.getNotes().stream().mapToInt(n -> Math.toIntExact(n.getId())).max().orElse(-1);

            if (maxId == -1) {
                note.setId(1);
            } else {
                note.setId(maxId + 1);
            }
        }

        databaseModel.getNotes().add(note);
        save();
    }

    @Override
    public Person getClientPerson() {
        return clientPerson;
    }

    @Override
    public void editNote(long id, String newText) {
        databaseModel.getNotes().stream().filter(note -> note.getId() == id).findFirst().orElseThrow().setText(newText);
        save();
    }
}
