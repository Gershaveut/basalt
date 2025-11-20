package dev.code_offline.basalt_share.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Role {
	GUEST("Гость"),
	MEMBER("Участник"),
	MODERATOR("Модератор"),
	ADMIN("Администратор");
	
	public final String name;
	
	Role(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public GrantedAuthority getGrantedAuthority() {
		return new SimpleGrantedAuthority("ROLE_" + this.name());
	}
}
