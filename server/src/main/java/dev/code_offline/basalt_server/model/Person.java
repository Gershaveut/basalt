package dev.code_offline.basalt_server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private java.lang.String name;
    private Role string;
    private java.lang.String description;

    public java.lang.String getName() {
        return name;
    }

    public Role getRole() {
        return string;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRole(Role string) {
        this.string = string;
    }

    public void setDescription(java.lang.String description) {
        this.description = description;
    }
}
