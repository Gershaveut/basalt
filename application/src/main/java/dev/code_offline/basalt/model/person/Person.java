package dev.code_offline.basalt.model.person;

import org.springframework.lang.Nullable;

public class Person {
    private long id;
    private String username;
    private String password;
    private Role role;
    @Nullable
    private String description;

    protected Person() {
    }
    
    public Person(long id, String username, String password, Role role, @Nullable String description) {
        this.id = id;
        this.username = username;
		this.password = password;
		this.role = role;
        this.description = description;
    }

    public Person(String username, String password, Role role) {
        this(0, username, password, role, null);
    }
    
    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
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
    
    @Override
    public String toString() {
        return username;
    }
}
