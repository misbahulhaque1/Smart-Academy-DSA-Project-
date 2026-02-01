package model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Course {
    private int id;
    private String title;
    private String description;
    private boolean isPaid;
    private double price;
    private int enrolledCount;
    private int teacherId;
    private Timestamp createdAt;

    public Course() {}

    public Course(String title, String description, boolean isPaid, double price, int teacherId) {
        this.title = title;
        this.description = description;
        this.isPaid = isPaid;
        this.price = price;
        this.teacherId = teacherId;
        this.enrolledCount = 0;
    }

    // build from DB
    public static Course fromResultSet(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setId(rs.getInt("id"));
        course.setTitle(rs.getString("title"));
        course.setDescription(rs.getString("description"));
        course.setPaid(rs.getBoolean("is_paid"));
        course.setPrice(rs.getDouble("price"));
        course.setEnrolledCount(rs.getInt("enrolled_count"));
        course.setTeacherId(rs.getInt("teacher_id"));
        course.setCreatedAt(rs.getTimestamp("created_at"));
        return course;
    }

    // getters / setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(int enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isPaid=" + isPaid +
                ", price=" + price +
                ", enrolledCount=" + enrolledCount +
                '}';
    }
}
