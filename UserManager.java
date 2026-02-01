package manager;

import java.sql.*;
import model.*;
import persistence.DBConnection;
import util.HashUtil;

public class UserManager {

    // register
    public static boolean register(User user) {
        String sql = "INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getRole().name());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("User registered successfully: " + user.getEmail());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Registration failed: " + e.getMessage());
        }
        return false;
    }

    // login by email + password
    public static User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (HashUtil.verifyPassword(password, storedHash)) {
                    User user = User.fromResultSet(rs);
                    System.out.println("Login successful: " + user.getName() + " (" + user.getRole() + ")");
                    return user;
                } else {
                    System.err.println("Invalid password");
                }
            } else {
                System.err.println("User not found: " + email);
            }
        } catch (SQLException e) {
            System.err.println("Login failed: " + e.getMessage());
        }
        return null;
    }

    // find by email
    public static User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return User.fromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        return null;
    }

    // find by id
    public static User findById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return User.fromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user: " + e.getMessage());
        }
        return null;
    }

    // update basic info
    public static boolean updateUser(User user) {
        String sql = "UPDATE users SET name = ?, email = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setInt(3, user.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
        return false;
    }

    // change password
    public static boolean changePassword(int userId, String oldPassword, String newPassword) {
        User user = findById(userId);

        if (user != null && HashUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
            String sql = "UPDATE users SET password_hash = ? WHERE id = ?";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, HashUtil.hashPassword(newPassword));
                pstmt.setInt(2, userId);

                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            } catch (SQLException e) {
                System.err.println("Error changing password: " + e.getMessage());
            }
        }
        return false;
    }
}
