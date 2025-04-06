package dev.code_offline.basalt.model.user;

import org.checkerframework.checker.nullness.qual.Nullable;

public class User {
    private final String name;
    private final int id;
    private final Role role;
    @Nullable
    private final String description;

    public User(String name, int id, Role role, @Nullable String description) {
        this.name = name;
        this.id = id;
        this.role = role;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public @Nullable String getDescription() {
        return description;
    }
}
