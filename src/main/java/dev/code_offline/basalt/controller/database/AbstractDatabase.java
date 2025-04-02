package dev.code_offline.basalt.controller.database;

import dev.code_offline.basalt.model.note.Note;
import dev.code_offline.basalt.model.user.Role;
import dev.code_offline.basalt.model.user.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDatabase {
    private Connection connection;

    private AbstractDatabase(Connection connection) {
        this.connection = connection;
    }

    public List<User> getUsers() throws SQLException {
        var users = new ArrayList<User>();

        var userTable = connection.createStatement().executeQuery("SELECT * FROM user");

        while (userTable.next()) {
            users.add(new User(userTable.getString("name"), userTable.getInt("id"), Role.valueOf(userTable.getString("role")), userTable.getString("description")));
        }

        return users;
    }

    public List<Note> getNotes() throws SQLException {
        var users = getUsers();
        var notes = new ArrayList<Note>();

        var noteTable = connection.createStatement().executeQuery("SELECT * FROM note");

        while (noteTable.next()) {
            User user;

            user = users.stream().filter(usr -> {
                try {
                    return usr.getId() == noteTable.getInt("user");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).findFirst().orElseThrow();

            notes.add(new Note(noteTable.getString("name"), user, noteTable.getString("text")));
        }

        return notes;
    }
}
