package org.gershaveut.basalt_share.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
public class Person implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private @NotNull String username;
    private @NotNull String password;
    private @NotNull Role role;
    private @Nullable String description;
	
	private Person() {
        this.username = "Null";
        this.password = username;
        this.role = Role.GUEST;
    }
	
	public Person(String username, String password, Role role, @Nullable String description) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.description = description;
    }
    
    
    public Person(String username, String password, Role role) {
       this(username, password, role, null);
    }
    
    public long getId() {
        return id;
    }
   
    @Override
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
  
    @Override
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    @Nullable
    public String getDescription() {
        return description;
    }
    
    public void setDescription(@Nullable String description) {
        this.description = description;
    }
  
    @Override
    public String toString() {
        return username;
    }
    
    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(role.getGrantedAuthority());
    }
}
