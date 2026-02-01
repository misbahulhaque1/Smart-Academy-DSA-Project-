package model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Note {
    private int id;
    private int courseId;
    private int teacherId;
    private String title;
    private String filePath;
    private boolean isFree;
    private Timestamp uploadTime;

    public Note() {}

    public Note(int courseId, int teacherId, String title, String filePath, boolean isFree) {
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.title = title;
        this.filePath = filePath;
        this.isFree = isFree;
    }

    // build from DB
    public static Note fromResultSet(ResultSet rs) throws SQLException {
        Note note = new Note();
        note.setId(rs.getInt("id"));
        note.setCourseId(rs.getInt("course_id"));
        note.setTeacherId(rs.getInt("teacher_id"));
        note.setTitle(rs.getString("title"));
        note.setFilePath(rs.getString("file_path"));
        note.setFree(rs.getBoolean("is_free"));
        note.setUploadTime(rs.getTimestamp("upload_time"));
        return note;
    }

    // getters / setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean free) {
        isFree = free;
    }

    public Timestamp getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Timestamp uploadTime) {
        this.uploadTime = uploadTime;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", courseId=" + courseId +
                ", title='" + title + '\'' +
                ", isFree=" + isFree +
                '}';
    }
}
