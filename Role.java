package model;

public enum Role {
    STUDENT,
    TEACHER,
    ADMIN;

    // convert string to enum
    public static Role fromString(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (Exception e) {
            return STUDENT; // default
        }
    }
}
