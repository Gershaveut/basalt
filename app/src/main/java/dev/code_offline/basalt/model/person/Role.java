package dev.code_offline.basalt.model.person;

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
}
