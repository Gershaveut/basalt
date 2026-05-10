package org.gershaveut.basalt_share.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private @NotNull long file;
    @ManyToOne
    private @NotNull Person person;
    private @NotNull String text;
    private @LastModifiedDate @Nullable LocalDateTime lastUpdated;
    private @NotNull boolean edited;

    private Comment() {
        this.text = "";
    }
    
    public Comment(long file, Person person, String text) {
        this.file = file;
        this.person = person;
        this.text = text;
    }

    public long getId() {
        return id;
    }

    @JsonIgnore
    public long getFile() {
        return file;
    }

    public Person getPerson() {
        return person;
    }

    public String getText() {
        return text;
    }

    public void setText(@NotNull String text) {
        this.text = text;
    }

    public @Nullable LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public boolean isEdited() {
        return edited;
    }
    
    public void setEdited(@NotNull boolean edited) {
        this.edited = edited;
    }
}
