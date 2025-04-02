package dev.code_offline.basalt.model.user;

public class User {
    private String name;
    private int id;
    private Role role;
    private String description;

    public User(String name, int id, Role role, String description) {
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

    public String getDescription() {
        return description;
    }
}
