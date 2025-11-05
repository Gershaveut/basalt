package dev.code_offline.basalt_server.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Role {
    GUEST,
    MEMBER,
    MODERATOR,
    ADMIN;
    
    public final GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_" + this.name());
}
