package dev.code_offline.basalt_server.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
public class Person implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;
    private @NotNull String password;
    private @NotNull Role role;
    private String description;
	
	public Person() {
	}
	
	public Person(String username, String password, Role role, @Nullable String description) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.description = description;
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
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(role.grantedAuthority);
    }
}
