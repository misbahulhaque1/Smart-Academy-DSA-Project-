package persistence;

import util.Constants;
import java.io.*;
import java.nio.file.*;

public class FileHandler {

    // copy note file
    public static String copyNoteFile(File sourceFile, String fileName) {
        try {
            File notesDir = new File(Constants.NOTES_DIRECTORY);
            if (!notesDir.exists()) {
                notesDir.mkdirs();
            }

            Path targetPath = Paths.get(Constants.NOTES_DIRECTORY + fileName);
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toString();

        } catch (IOException e) {
            System.err.println("Error copying file: " + e.getMessage());
            return null;
        }
    }

    // delete note file
    public static boolean deleteNoteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting file: " + e.getMessage());
            return false;
        }
    }

    // check file exists
    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }
}
