package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification using SHA-256
 */
public class HashUtil {

    private static final String ALGORITHM = "SHA-256";

    /**
     * Hash a password using SHA-256
     * @param password Plain text password
     * @return Hashed password as hexadecimal string
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verify password against hash
     * @param password Plain text password to verify
     * @param hash Stored password hash
     * @return true if password matches hash
     */
    public static boolean verifyPassword(String password, String hash) {
        String computedHash = hashPassword(password);
        return computedHash.equals(hash);
    }

    /**
     * Convert byte array to hexadecimal string
     * @param bytes Byte array
     * @return Hexadecimal string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Generate random salt for additional security (optional enhancement)
     * @return Base64 encoded salt
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
