package model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Payment {
    private int id;
    private int studentId;
    private int courseId;
    private double amount;
    private PaymentStatus status;
    private Timestamp time;
    private Timestamp processedAt;

    // status enum
    public enum PaymentStatus {
        PENDING,
        APPROVED,
        REJECTED;

        public static PaymentStatus fromString(String status) {
            try {
                return PaymentStatus.valueOf(status.toUpperCase());
            } catch (Exception e) {
                return PENDING;
            }
        }
    }

    public Payment() {}

    public Payment(int studentId, int courseId, double amount) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
    }

    // from db
    public static Payment fromResultSet(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getInt("id"));
        payment.setStudentId(rs.getInt("student_id"));
        payment.setCourseId(rs.getInt("course_id"));
        payment.setAmount(rs.getDouble("amount"));
        payment.setStatus(PaymentStatus.fromString(rs.getString("status")));
        payment.setTime(rs.getTimestamp("time"));
        payment.setProcessedAt(rs.getTimestamp("processed_at"));
        return payment;
    }

    // getters/setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public Timestamp getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Timestamp processedAt) {
        this.processedAt = processedAt;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", amount=" + amount +
                ", status=" + status +
                '}';
    }
}
