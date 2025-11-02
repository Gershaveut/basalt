package dev.code_offline.basalt.model.person;

import org.springframework.lang.Nullable;

public class Person {
    private long id;
    private final String username;
    private final Role role;
    @Nullable
    private final String description;

    public Person(String username, int id, Role role, @Nullable String description) {
        this.username = username;
        this.id = id;
        this.role = role;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public void setId(long id) {
        this.id = id;
    }
}
