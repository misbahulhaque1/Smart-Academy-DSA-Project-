package ui;

import dsas.DSAUtils;
import java.sql.*;
import java.util.*;
import manager.*;
import model.*;
import persistence.DBConnection;

public class ChartDataProvider {

    // enroll stats (top 10)
    public static Map<String, Integer> getEnrollmentStatistics() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String sql = "SELECT title, enrolled_count FROM courses ORDER BY enrolled_count DESC LIMIT 10";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                stats.put(rs.getString("title"), rs.getInt("enrolled_count"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching enrollment stats: " + e.getMessage());
        }
        return stats;
    }

    // revenue per course (top 10)
    public static Map<String, Double> getRevenueStatistics() {
        Map<String, Double> stats = new LinkedHashMap<>();
        String sql = "SELECT c.title, SUM(p.amount) as revenue " +
                "FROM courses c " +
                "JOIN payments p ON c.id = p.course_id " +
                "WHERE p.status = 'APPROVED' " +
                "GROUP BY c.id " +
                "ORDER BY revenue DESC " +
                "LIMIT 10";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                stats.put(rs.getString("title"), rs.getDouble("revenue"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching revenue stats: " + e.getMessage());
        }
        return stats;
    }

    // payment status counts
    public static Map<String, Integer> getPaymentStatusDistribution() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) as count FROM payments GROUP BY status";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                stats.put(rs.getString("status"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching payment status: " + e.getMessage());
        }
        return stats;
    }

    // user roles
    public static Map<String, Integer> getUserRoleDistribution() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String sql = "SELECT role, COUNT(*) as count FROM users GROUP BY role";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                stats.put(rs.getString("role"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user role distribution: " + e.getMessage());
        }
        return stats;
    }

    // last 12 months enrollments
    public static Map<String, Integer> getMonthlyEnrollmentTrend() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(timestamp, '%Y-%m') as month, COUNT(*) as count " +
                "FROM enrollments " +
                "WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 12 MONTH) " +
                "GROUP BY month " +
                "ORDER BY month";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                stats.put(rs.getString("month"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching enrollment trend: " + e.getMessage());
        }
        return stats;
    }

    // totals for dashboard
    public static Map<String, Object> getTotalStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next()) stats.put("totalUsers", rs.getInt(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM courses");
            if (rs.next()) stats.put("totalCourses", rs.getInt(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM enrollments WHERE approved = TRUE");
            if (rs.next()) stats.put("totalEnrollments", rs.getInt(1));

            rs = stmt.executeQuery("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status = 'APPROVED'");
            if (rs.next()) stats.put("totalRevenue", rs.getDouble(1));

            rs = stmt.executeQuery("SELECT COUNT(*) FROM payments WHERE status = 'PENDING'");
            if (rs.next()) stats.put("pendingPayments", rs.getInt(1));

        } catch (SQLException e) {
            System.err.println("Error fetching total statistics: " + e.getMessage());
        }
        return stats;
    }

    // top N by enrolled
    public static List<Course> getTopCoursesByEnrollment(int limit) {
        List<Course> courses = CourseManager.getAllCourses();
        DSAUtils.sortCoursesByEnrollment(courses);
        return courses.subList(0, Math.min(limit, courses.size()));
    }

    // courses in price range (sorted)
    public static List<Course> getCoursesByPriceRange(double minPrice, double maxPrice) {
        List<Course> allCourses = CourseManager.getAllCourses();
        List<Course> filtered = new ArrayList<>();

        for (Course course : allCourses) {
            if (course.getPrice() >= minPrice && course.getPrice() <= maxPrice) {
                filtered.add(course);
            }
        }

        DSAUtils.sortCoursesByPrice(filtered);
        return filtered;
    }

    // teacher stats
    public static Map<String, Object> getTeacherStatistics(int teacherId) {
        Map<String, Object> stats = new HashMap<>();

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM courses WHERE teacher_id = ?");
            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) stats.put("totalCourses", rs.getInt(1));

            pstmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM enrollments e " +
                            "JOIN courses c ON e.course_id = c.id " +
                            "WHERE c.teacher_id = ? AND e.approved = TRUE"
            );
            pstmt.setInt(1, teacherId);
            rs = pstmt.executeQuery();
            if (rs.next()) stats.put("totalEnrollments", rs.getInt(1));

            pstmt = conn.prepareStatement("SELECT COUNT(*) FROM notes WHERE teacher_id = ?");
            pstmt.setInt(1, teacherId);
            rs = pstmt.executeQuery();
            if (rs.next()) stats.put("totalNotes", rs.getInt(1));

            stats.put("averageRating", 0.0); // placeholder

        } catch (SQLException e) {
            System.err.println("Error fetching teacher statistics: " + e.getMessage());
        }
        return stats;
    }

    // student stats
    public static Map<String, Object> getStudentStatistics(int studentId) {
        Map<String, Object> stats = new HashMap<>();

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND approved = TRUE"
            );
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) stats.put("enrolledCourses", rs.getInt(1));

            pstmt = conn.prepareStatement(
                    "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE student_id = ? AND status = 'APPROVED'"
            );
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();
            if (rs.next()) stats.put("totalPaid", rs.getDouble(1));

            pstmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM payments WHERE student_id = ? AND status = 'PENDING'"
            );
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();
            if (rs.next()) stats.put("pendingPayments", rs.getInt(1));

        } catch (SQLException e) {
            System.err.println("Error fetching student statistics: " + e.getMessage());
        }
        return stats;
    }
}
