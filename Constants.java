package util;

/**
 * Application-wide constants for database configuration and other settings
 */
public class Constants {
    // Database Configuration
    public static final String DB_URL = "jdbc:mysql://localhost:3306/smartacademy";
    public static final String DB_USER = "root";
    public static final String DB_PASS = "12345";
    public static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    // File Storage Paths
    public static final String NOTES_DIRECTORY = System.getProperty("user.dir") + "/notes/";

    // Application Settings
    public static final int SESSION_TIMEOUT = 3600; // seconds
    public static final int MAX_LOGIN_ATTEMPTS = 3;

    // Private constructor to prevent instantiation
    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }
}
