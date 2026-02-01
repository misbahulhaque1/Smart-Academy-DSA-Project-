package manager;

import dsas.StackDS;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Note;
import persistence.DBConnection;
import persistence.FileHandler;

public class NoteManager {

    // track uploads for undo
    private static StackDS<Integer> uploadStack = new StackDS<>();

    // upload
    public static int uploadNote(Note note) {
        String sql = "INSERT INTO notes (course_id, teacher_id, title, file_path, is_free) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, note.getCourseId());
            pstmt.setInt(2, note.getTeacherId());
            pstmt.setString(3, note.getTitle());
            pstmt.setString(4, note.getFilePath());
            pstmt.setBoolean(5, note.isFree());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int noteId = rs.getInt(1);
                    note.setId(noteId);
                    uploadStack.push(noteId); // for undo
                    System.out.println("Note uploaded: " + note.getTitle());
                    return noteId;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error uploading note: " + e.getMessage());
        }
        return -1;
    }

    // undo last upload
    public static boolean undoLastUpload(int teacherId) {
        if (uploadStack.isEmpty()) {
            System.err.println("No notes to undo");
            return false;
        }

        int noteId = uploadStack.pop();
        Note note = getNoteById(noteId);
        if (note != null && note.getTeacherId() == teacherId) {
            return deleteNote(noteId, false); // no stack change here
        } else {
            System.err.println("Cannot undo: Note not owned by teacher or not found");
            uploadStack.push(noteId); // put back
            return false;
        }
    }

    // notes by course
    public static List<Note> getNotesByCourse(int courseId) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes WHERE course_id = ? ORDER BY upload_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                notes.add(Note.fromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching notes: " + e.getMessage());
        }
        return notes;
    }

    // get by id
    public static Note getNoteById(int noteId) {
        String sql = "SELECT * FROM notes WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, noteId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Note.fromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching note: " + e.getMessage());
        }
        return null;
    }

    // notes by teacher
    public static List<Note> getNotesByTeacher(int teacherId) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes WHERE teacher_id = ? ORDER BY upload_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                notes.add(Note.fromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching notes by teacher: " + e.getMessage());
        }
        return notes;
    }

    // free notes
    public static List<Note> getFreeNotes(int courseId) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes WHERE course_id = ? AND is_free = TRUE ORDER BY upload_time DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                notes.add(Note.fromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching free notes: " + e.getMessage());
        }
        return notes;
    }

    // update
    public static boolean updateNote(Note note) {
        String sql = "UPDATE notes SET title = ?, is_free = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, note.getTitle());
            pstmt.setBoolean(2, note.isFree());
            pstmt.setInt(3, note.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating note: " + e.getMessage());
        }
        return false;
    }

    // delete (internal)
    public static boolean deleteNote(int noteId, boolean removeFromStack) {
        Note note = getNoteById(noteId);
        if (note == null) return false;

        String sql = "DELETE FROM notes WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, noteId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                if (note.getFilePath() != null && !note.getFilePath().isEmpty()) {
                    FileHandler.deleteNoteFile(note.getFilePath()); // delete file
                }
                System.out.println("Note deleted: " + note.getTitle());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting note: " + e.getMessage());
        }
        return false;
    }

    // delete (public)
    public static boolean deleteNote(int noteId) {
        return deleteNote(noteId, true);
    }

    // access check
    public static boolean canAccessNote(int studentId, int noteId) {
        Note note = getNoteById(noteId);
        if (note == null) return false;

        if (note.isFree()) return true;

        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND course_id = ? AND approved = TRUE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, note.getCourseId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking note access: " + e.getMessage());
        }
        return false;
    }

    // undo stack size
    public static int getUndoStackSize() {
        return uploadStack.size();
    }
}
