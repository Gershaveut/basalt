package dev.code_offline.basalt.model.person;

import org.checkerframework.checker.nullness.qual.Nullable;

public class Person {
    private long id;
    private final String name;
    private final Role role;
    @Nullable
    private final String description;

    public Person(String name, int id, Role role, @Nullable String description) {
        this.name = name;
        this.id = id;
        this.role = role;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
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
