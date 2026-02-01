package model;

public class Student extends User {

    public Student() {
        super();
        setRole(Role.STUDENT);
    }

    public Student(int id, String name, String email, String passwordHash) {
        super(id, name, email, passwordHash, Role.STUDENT);
    }

    public Student(String name, String email, String passwordHash) {
        super(name, email, passwordHash, Role.STUDENT);
    }
}
