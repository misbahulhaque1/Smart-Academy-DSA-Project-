package manager;

import dsas.GraphDS;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Course;
import persistence.DBConnection;

public class RecommendationEngine {

    private static GraphDS courseGraph = new GraphDS();
    private static boolean graphInitialized = false;

    // build from DB
    public static void buildGraphFromDB() {
        courseGraph.clear();

        String sql = "SELECT course_id, related_course_id, weight FROM recommendations";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int courseId = rs.getInt("course_id");
                int relatedCourseId = rs.getInt("related_course_id");
                double weight = rs.getDouble("weight");
                courseGraph.addEdge(courseId, relatedCourseId, weight);
            }

            graphInitialized = true;
            System.out.println("Course recommendation graph built with " +
                    courseGraph.getVertexCount() + " courses");

        } catch (SQLException e) {
            System.err.println("Error building graph: " + e.getMessage());
        }
    }

    // ensure built
    private static void ensureGraphInitialized() {
        if (!graphInitialized) {
            buildGraphFromDB();
        }
    }

    // recommend for student
    public static List<Course> recommendForStudent(int studentId, int maxRecommendations) {
        ensureGraphInitialized();

        List<Course> recommendations = new ArrayList<>();
        List<Course> enrolledCourses = CourseManager.getEnrolledCourses(studentId);

        if (enrolledCourses.isEmpty()) {
            return getPopularCourses(maxRecommendations);
        }

        List<Integer> recommendedIds = new ArrayList<>();

        for (Course course : enrolledCourses) {
            List<Integer> courseRecs = courseGraph.getRecommendations(course.getId(), 2);
            for (Integer recId : courseRecs) {
                if (!recommendedIds.contains(recId) && !isEnrolledInCourse(enrolledCourses, recId)) {
                    recommendedIds.add(recId);
                }
            }
        }

        for (int i = 0; i < Math.min(recommendedIds.size(), maxRecommendations); i++) {
            Course course = CourseManager.getCourseById(recommendedIds.get(i));
            if (course != null) {
                recommendations.add(course);
            }
        }

        if (recommendations.size() < maxRecommendations) {
            List<Course> popular = getPopularCourses(maxRecommendations - recommendations.size());
            for (Course course : popular) {
                if (!recommendations.contains(course) && !isEnrolledInCourse(enrolledCourses, course.getId())) {
                    recommendations.add(course);
                    if (recommendations.size() >= maxRecommendations) break;
                }
            }
        }

        return recommendations;
    }

    // direct related
    public static List<Course> getRelatedCourses(int courseId, int maxRecommendations) {
        ensureGraphInitialized();

        List<Course> recommendations = new ArrayList<>();
        List<Integer> recommendedIds = courseGraph.getDirectRecommendations(courseId);

        for (int i = 0; i < Math.min(recommendedIds.size(), maxRecommendations); i++) {
            Course course = CourseManager.getCourseById(recommendedIds.get(i));
            if (course != null) {
                recommendations.add(course);
            }
        }
        return recommendations;
    }

    // add edge
    public static boolean addRecommendation(int courseId, int relatedCourseId, double weight) {
        String sql = "INSERT INTO recommendations (course_id, related_course_id, weight) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE weight = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            pstmt.setInt(2, relatedCourseId);
            pstmt.setDouble(3, weight);
            pstmt.setDouble(4, weight);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                courseGraph.addEdge(courseId, relatedCourseId, weight);
                System.out.println("Recommendation added: " + courseId + " -> " + relatedCourseId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding recommendation: " + e.getMessage());
        }
        return false;
    }

    // remove edge
    public static boolean removeRecommendation(int courseId, int relatedCourseId) {
        String sql = "DELETE FROM recommendations WHERE course_id = ? AND related_course_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            pstmt.setInt(2, relatedCourseId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                graphInitialized = false; // rebuild later
                System.out.println("Recommendation removed: " + courseId + " -> " + relatedCourseId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error removing recommendation: " + e.getMessage());
        }
        return false;
    }

    // popular (by enrolled_count)
    private static List<Course> getPopularCourses(int limit) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY enrolled_count DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                courses.add(Course.fromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching popular courses: " + e.getMessage());
        }
        return courses;
    }

    // enrolled check
    private static boolean isEnrolledInCourse(List<Course> enrolledCourses, int courseId) {
        for (Course course : enrolledCourses) {
            if (course.getId() == courseId) return true;
        }
        return false;
    }

    // trending (last 30 days)
    public static List<Course> getTrendingCourses(int limit) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.*, COUNT(e.student_id) as recent_enrollments " +
                "FROM courses c " +
                "JOIN enrollments e ON c.id = e.course_id " +
                "WHERE e.timestamp >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                "GROUP BY c.id " +
                "ORDER BY recent_enrollments DESC " +
                "LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                courses.add(Course.fromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching trending courses: " + e.getMessage());
        }
        return courses;
    }

    // force rebuild
    public static void rebuildGraph() {
        graphInitialized = false;
        buildGraphFromDB();
    }
}
