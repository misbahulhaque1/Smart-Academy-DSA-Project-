package model;

public class Teacher extends User {

    public Teacher() {
        super();
        setRole(Role.TEACHER);
    }

    public Teacher(int id, String name, String email, String passwordHash) {
        super(id, name, email, passwordHash, Role.TEACHER);
    }

    public Teacher(String name, String email, String passwordHash) {
        super(name, email, passwordHash, Role.TEACHER);
    }
}