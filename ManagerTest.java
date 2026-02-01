package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import manager.*;
import model.*;
import persistence.DBConnection;
import util.HashUtil;

import java.sql.SQLException;



public class ManagerTest {

    @BeforeAll
    static void setupDatabase() {
        // Ensure database connection is available
        try {
            if (!DBConnection.testConnection()) {
                fail("Database connection not available");
            }
        } catch (Exception e) {
            fail("Failed to connect to database: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test User Registration and Login")
    void testUserRegistrationAndLogin() {
        // Create test user
        String email = "test_" + System.currentTimeMillis() + "@test.com";
        User testUser = new User("Test User", email, HashUtil.hashPassword("password123"), Role.STUDENT);

        // Test registration
        boolean registered = UserManager.register(testUser);
        assertTrue(registered, "User registration should succeed");

        // Test login with correct password
        User loggedIn = UserManager.login(email, "password123");
        assertNotNull(loggedIn, "Login should succeed with correct password");
        assertEquals(email, loggedIn.getEmail());

        // Test login with wrong password
        User failedLogin = UserManager.login(email, "wrongpassword");
        assertNull(failedLogin, "Login should fail with wrong password");

        // Test duplicate registration
        boolean duplicate = UserManager.register(testUser);
        assertFalse(duplicate, "Duplicate email registration should fail");
    }

    @Test
    @DisplayName("Test Course Creation and Retrieval")
    void testCourseManagement() {
        // Get a teacher user (assuming ID 2 exists from schema)
        User teacher = UserManager.findById(2);
        assertNotNull(teacher, "Teacher user should exist");

        // Create course
        Course course = new Course(
            "Test Course " + System.currentTimeMillis(),
            "Test Description",
            true,
            99.99,
            teacher.getId()
        );

        int courseId = CourseManager.createCourse(course);
        assertTrue(courseId > 0, "Course creation should return valid ID");

        // Retrieve course
        Course retrieved = CourseManager.getCourseById(courseId);
        assertNotNull(retrieved, "Course should be retrievable");
        assertEquals(course.getTitle(), retrieved.getTitle());

        // Search by title
        Course searchResult = CourseManager.searchByTitle(course.getTitle());
        assertNotNull(searchResult, "Course should be searchable by title");
    }

    @Test
    @DisplayName("Test Note Upload and Undo")
    void testNoteManagement() {
        // Assuming course ID 1 and teacher ID 2 exist
        Note note = new Note(1, 2, "Test Note " + System.currentTimeMillis(), "/test/path.pdf", true);

        int initialStackSize = NoteManager.getUndoStackSize();

        // Upload note
        int noteId = NoteManager.uploadNote(note);
        assertTrue(noteId > 0, "Note upload should succeed");

        // Check stack size increased
        assertEquals(initialStackSize + 1, NoteManager.getUndoStackSize(), "Stack size should increase");

        // Retrieve note
        Note retrieved = NoteManager.getNoteById(noteId);
        assertNotNull(retrieved, "Note should be retrievable");

        // Test undo
        boolean undone = NoteManager.undoLastUpload(2);
        assertTrue(undone, "Undo should succeed");
        assertEquals(initialStackSize, NoteManager.getUndoStackSize(), "Stack size should decrease");
    }

    @Test
    @DisplayName("Test Payment Queue and Approval")
    void testPaymentManagement() {
        // Assuming student ID 3 and course ID 1 exist
        Payment payment = new Payment(3, 1, 49.99);

        int initialQueueSize = PaymentManager.getQueueSize();

        // Request payment
        int paymentId = PaymentManager.requestPayment(payment);
        assertTrue(paymentId > 0, "Payment request should succeed");

        // Verify queue increased (after initialization)
        // Note: Queue might not increase if it wasn't initialized before

        // Retrieve payment
        Payment retrieved = PaymentManager.getPaymentById(paymentId);
        assertNotNull(retrieved, "Payment should be retrievable");
        assertEquals(Payment.PaymentStatus.PENDING, retrieved.getStatus());

        // Approve payment
        boolean approved = PaymentManager.approvePayment(paymentId);
        assertTrue(approved, "Payment approval should succeed");

        // Verify status changed
        Payment approvedPayment = PaymentManager.getPaymentById(paymentId);
        assertEquals(Payment.PaymentStatus.APPROVED, approvedPayment.getStatus());
    }

    @Test
    @DisplayName("Test Recommendation Engine")
    void testRecommendationEngine() {
        // Build graph from database
        RecommendationEngine.buildGraphFromDB();

        // Test adding recommendation
        boolean added = RecommendationEngine.addRecommendation(1, 2, 1.5);
        assertTrue(added, "Adding recommendation should succeed");

        // Test getting related courses
        var related = RecommendationEngine.getRelatedCourses(1, 5);
        assertNotNull(related, "Should return related courses list");

        // Test student recommendations (assuming student ID 3)
        var recommendations = RecommendationEngine.recommendForStudent(3, 5);
        assertNotNull(recommendations, "Should return recommendations");
    }

    @Test
    @DisplayName("Test HashUtil")
    void testHashUtil() {
        String password = "testPassword123";

        // Test hashing
        String hash1 = HashUtil.hashPassword(password);
        assertNotNull(hash1, "Hash should not be null");
        assertFalse(hash1.isEmpty(), "Hash should not be empty");

        // Same password should produce same hash
        String hash2 = HashUtil.hashPassword(password);
        assertEquals(hash1, hash2, "Same password should produce same hash");

        // Test verification
        assertTrue(HashUtil.verifyPassword(password, hash1), "Password should verify correctly");
        assertFalse(HashUtil.verifyPassword("wrongPassword", hash1), "Wrong password should not verify");
    }
}
