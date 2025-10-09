package dev.code_offline.basalt_server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.lang.Nullable;

@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;
    private Role string;
    private @Nullable String description;
    
    public long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }

    public Role getRole() {
        return string;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRole(Role string) {
        this.string = string;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }
}
