package manager;

import dsas.BSTCourse;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Course;
import persistence.DBConnection;

public class CourseManager {

    private static BSTCourse courseBST = new BSTCourse();
    private static boolean bstInitialized = false;

    // init BST once
    private static void initializeBST() {
        if (!bstInitialized) {
            courseBST.clear();
            List<Course> courses = getAllCourses();
            for (Course course : courses) {
                courseBST.insert(course);
            }
            bstInitialized = true;
        }
    }

    // create
    public static int createCourse(Course course) {
        String sql = "INSERT INTO courses (title, description, is_paid, price, teacher_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, course.getTitle());
            pstmt.setString(2, course.getDescription());
            pstmt.setBoolean(3, course.isPaid());
            pstmt.setDouble(4, course.getPrice());
            pstmt.setInt(5, course.getTeacherId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int courseId = rs.getInt(1);
                    course.setId(courseId);
                    // cache insert
                    courseBST.insert(course);
                    System.out.println("Course created: " + course.getTitle());
                    return courseId;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating course: " + e.getMessage());
        }
        return -1;
    }

    // read all
    public static List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                courses.add(Course.fromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching courses: " + e.getMessage());
        }
        return courses;
    }

    // search exact title via BST
    public static Course searchByTitle(String title) {
        initializeBST();
        return courseBST.search(title);
    }

    // search partial title via BST
    public static List<Course> searchByPartialTitle(String partialTitle) {
        initializeBST();
        return courseBST.searchByPartialTitle(partialTitle);
    }

    // read by id
    public static Course getCourseById(int courseId) {
        String sql = "SELECT * FROM courses WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Course.fromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching course: " + e.getMessage());
        }
        return null;
    }

    // read by teacher
    public static List<Course> getCoursesByTeacher(int teacherId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses WHERE teacher_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                courses.add(Course.fromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching courses by teacher: " + e.getMessage());
        }
        return courses;
    }

    // read by type
    public static List<Course> getCoursesByType(boolean isPaid) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses WHERE is_paid = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, isPaid);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                courses.add(Course.fromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching courses by type: " + e.getMessage());
        }
        return courses;
    }

    // update
    public static boolean updateCourse(Course course) {
        String sql = "UPDATE courses SET title = ?, description = ?, is_paid = ?, price = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getTitle());
            pstmt.setString(2, course.getDescription());
            pstmt.setBoolean(3, course.isPaid());
            pstmt.setDouble(4, course.getPrice());
            pstmt.setInt(5, course.getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                bstInitialized = false; // refresh next time
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating course: " + e.getMessage());
        }
        return false;
    }

    // delete
    public static boolean deleteCourse(int courseId) {
        String sql = "DELETE FROM courses WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                bstInitialized = false; // refresh next time
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting course: " + e.getMessage());
        }
        return false;
    }

    // increment enrolled_count
    public static boolean incrementEnrollment(int courseId) {
        String sql = "UPDATE courses SET enrolled_count = enrolled_count + 1 WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                bstInitialized = false; // refresh later
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error incrementing enrollment: " + e.getMessage());
        }
        return false;
    }

    // enrolled courses for a student
    public static List<Course> getEnrolledCourses(int studentId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.* FROM courses c " +
                "JOIN enrollments e ON c.id = e.course_id " +
                "WHERE e.student_id = ? AND e.approved = TRUE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                courses.add(Course.fromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching enrolled courses: " + e.getMessage());
        }
        return courses;
    }
}
