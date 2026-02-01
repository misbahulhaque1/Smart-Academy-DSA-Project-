package manager;

import dsas.QueueDS;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Payment;
import model.Payment.PaymentStatus;
import persistence.DBConnection;

public class PaymentManager {

    // pending approvals (FIFO)
    private static QueueDS<Integer> paymentQueue = new QueueDS<>();
    private static boolean queueInitialized = false;

    // init queue once
    private static void initializeQueue() {
        if (!queueInitialized) {
            paymentQueue.clear();
            List<Payment> pendingPayments = getPendingPayments();
            for (Payment payment : pendingPayments) {
                paymentQueue.enqueue(payment.getId());
            }
            queueInitialized = true;
        }
    }

    // request payment
    public static int requestPayment(Payment payment) {
        String sql = "INSERT INTO payments (student_id, course_id, amount, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, payment.getStudentId());
            pstmt.setInt(2, payment.getCourseId());
            pstmt.setDouble(3, payment.getAmount());
            pstmt.setString(4, PaymentStatus.PENDING.name());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int paymentId = rs.getInt(1);
                    payment.setId(paymentId);
                    paymentQueue.enqueue(paymentId); // add to queue
                    System.out.println("Payment request created: #" + paymentId);
                    return paymentId;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error requesting payment: " + e.getMessage());
        }
        return -1;
    }

    // approve next in queue
    public static boolean approveNextPayment() {
        initializeQueue();
        if (paymentQueue.isEmpty()) {
            System.err.println("No pending payments to approve");
            return false;
        }
        int paymentId = paymentQueue.dequeue();
        return approvePayment(paymentId);
    }

    // approve by id
    public static boolean approvePayment(int paymentId) {
        String sql = "UPDATE payments SET status = ?, processed_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, PaymentStatus.APPROVED.name());
            pstmt.setInt(2, paymentId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                Payment payment = getPaymentById(paymentId);
                if (payment != null) {
                    enrollStudent(payment.getStudentId(), payment.getCourseId());
                    CourseManager.incrementEnrollment(payment.getCourseId());
                    System.out.println("Payment approved: #" + paymentId);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error approving payment: " + e.getMessage());
        }
        return false;
    }

    // reject by id
    public static boolean rejectPayment(int paymentId) {
        String sql = "UPDATE payments SET status = ?, processed_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, PaymentStatus.REJECTED.name());
            pstmt.setInt(2, paymentId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Payment rejected: #" + paymentId);
                queueInitialized = false; // rebuild later
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error rejecting payment: " + e.getMessage());
        }
        return false;
    }

    // enroll helper
    private static boolean enrollStudent(int studentId, int courseId) {
        String sql = "INSERT INTO enrollments (student_id, course_id, approved) VALUES (?, ?, TRUE) " +
                "ON DUPLICATE KEY UPDATE approved = TRUE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error enrolling student: " + e.getMessage());
        }
        return false;
    }

    // enroll directly (free)
    public static boolean enrollStudentDirectly(int studentId, int courseId) {
        boolean enrolled = enrollStudent(studentId, courseId);
        if (enrolled) {
            CourseManager.incrementEnrollment(courseId);
        }
        return enrolled;
    }

    // get payment
    public static Payment getPaymentById(int paymentId) {
        String sql = "SELECT * FROM payments WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, paymentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Payment.fromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching payment: " + e.getMessage());
        }
        return null;
    }

    // pending list
    public static List<Payment> getPendingPayments() {
        return getPaymentsByStatus(PaymentStatus.PENDING);
    }

    // by status
    public static List<Payment> getPaymentsByStatus(PaymentStatus status) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE status = ? ORDER BY time ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                payments.add(Payment.fromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching payments: " + e.getMessage());
        }
        return payments;
    }

    // by student
    public static List<Payment> getPaymentsByStudent(int studentId) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE student_id = ? ORDER BY time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                payments.add(Payment.fromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student payments: " + e.getMessage());
        }
        return payments;
    }

    // all payments (admin)
    public static List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments ORDER BY time DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                payments.add(Payment.fromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all payments: " + e.getMessage());
        }
        return payments;
    }

    // enrolled?
    public static boolean isEnrolled(int studentId, int courseId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND course_id = ? AND approved = TRUE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking enrollment: " + e.getMessage());
        }
        return false;
    }

    // queue size
    public static int getQueueSize() {
        initializeQueue();
        return paymentQueue.size();
    }
}
