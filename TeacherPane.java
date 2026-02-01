package ui;
import javafx.collections.*;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import manager.*;
import model.*;
import persistence.FileHandler;

import java.io.File;
import java.util.List;
import java.util.Map;

public class TeacherPane extends VBox {

    private User teacher;
    private MainApp mainApp;
    private TableView<Course> coursesTable;
    private TableView<Note> notesTable;

    public TeacherPane(User teacher, MainApp mainApp) {
        this.teacher = teacher;
        this.mainApp = mainApp;

        setPadding(new Insets(20));
        setSpacing(15);

        HBox header = createHeader();

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                createMyCoursesTab(),
                createNotesTab(),
                createStatisticsTab(),
                createProfileTab()
        );

        getChildren().addAll(header, tabPane);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #FF9800; -fx-background-radius: 5;");

        Label welcomeLabel = new Label("Teacher Dashboard - " + teacher.getName());
        welcomeLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: white; -fx-text-fill: #FF9800;");
        logoutButton.setOnAction(e -> mainApp.logout());

        header.getChildren().addAll(welcomeLabel, spacer, logoutButton);
        return header;
    }

    private Tab createMyCoursesTab() {
        Tab tab = new Tab("My Courses");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        HBox toolbar = new HBox(10);
        Button createCourseBtn = new Button("Create New Course");
        createCourseBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        createCourseBtn.setOnAction(e -> showCreateCourseDialog());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshCoursesTable());

        toolbar.getChildren().addAll(createCourseBtn, refreshBtn);

        coursesTable = new TableView<>();

        TableColumn<Course, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Course, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        TableColumn<Course, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(300);

        TableColumn<Course, Boolean> paidCol = new TableColumn<>("Type");
        paidCol.setCellValueFactory(new PropertyValueFactory<>("paid"));
        paidCol.setCellFactory(col -> new TableCell<Course, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Paid" : "Free");
                }
            }
        });
        paidCol.setPrefWidth(60);

        TableColumn<Course, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(80);

        TableColumn<Course, Integer> enrolledCol = new TableColumn<>("Enrolled");
        enrolledCol.setCellValueFactory(new PropertyValueFactory<>("enrolledCount"));
        enrolledCol.setPrefWidth(80);

        TableColumn<Course, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(col -> new TableCell<Course, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.setOnAction(e -> {
                    Course course = getTableView().getItems().get(getIndex());
                    showEditCourseDialog(course);
                });

                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                deleteBtn.setOnAction(e -> {
                    Course course = getTableView().getItems().get(getIndex());
                    deleteCourse(course);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, editBtn, deleteBtn));
            }
        });

        coursesTable.getColumns().addAll(idCol, titleCol, descCol, paidCol, priceCol, enrolledCol, actionCol);

        refreshCoursesTable();

        content.getChildren().addAll(toolbar, coursesTable);
        tab.setContent(content);
        return tab;
    }

    private void refreshCoursesTable() {
        List<Course> courses = CourseManager.getCoursesByTeacher(teacher.getId());
        coursesTable.setItems(FXCollections.observableArrayList(courses));
    }

    private void showCreateCourseDialog() {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Create New Course");
        dialog.setHeaderText("Enter course details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField();
        titleField.setPromptText("Course Title");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Course Description");
        descArea.setPrefRowCount(3);

        CheckBox paidCheckBox = new CheckBox("Paid Course");

        TextField priceField = new TextField("0.0");
        priceField.setPromptText("Price");
        priceField.setDisable(true);

        paidCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            priceField.setDisable(!newVal);
        });

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(paidCheckBox, 0, 2);
        grid.add(new Label("Price:"), 0, 3);
        grid.add(priceField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String title = titleField.getText().trim();
                String description = descArea.getText().trim();
                boolean isPaid = paidCheckBox.isSelected();
                double price = 0.0;

                if (isPaid) {
                    try {
                        price = Double.parseDouble(priceField.getText());
                    } catch (NumberFormatException e) {
                        showAlert("Error", "Invalid price format", Alert.AlertType.ERROR);
                        return null;
                    }
                }

                if (title.isEmpty()) {
                    showAlert("Error", "Title cannot be empty", Alert.AlertType.ERROR);
                    return null;
                }

                Course course = new Course(title, description, isPaid, price, teacher.getId());
                int courseId = CourseManager.createCourse(course);

                if (courseId > 0) {
                    showAlert("Success", "Course created successfully!", Alert.AlertType.INFORMATION);
                    refreshCoursesTable();
                    return course;
                } else {
                    showAlert("Error", "Failed to create course", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showEditCourseDialog(Course course) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit Course");
        dialog.setHeaderText("Update course details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField(course.getTitle());
        TextArea descArea = new TextArea(course.getDescription());
        descArea.setPrefRowCount(3);

        CheckBox paidCheckBox = new CheckBox("Paid Course");
        paidCheckBox.setSelected(course.isPaid());

        TextField priceField = new TextField(String.valueOf(course.getPrice()));
        priceField.setDisable(!course.isPaid());

        paidCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            priceField.setDisable(!newVal);
        });

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(paidCheckBox, 0, 2);
        grid.add(new Label("Price:"), 0, 3);
        grid.add(priceField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                course.setTitle(titleField.getText().trim());
                course.setDescription(descArea.getText().trim());
                course.setPaid(paidCheckBox.isSelected());

                if (course.isPaid()) {
                    try {
                        course.setPrice(Double.parseDouble(priceField.getText()));
                    } catch (NumberFormatException e) {
                        showAlert("Error", "Invalid price format", Alert.AlertType.ERROR);
                        return null;
                    }
                }

                if (CourseManager.updateCourse(course)) {
                    showAlert("Success", "Course updated successfully!", Alert.AlertType.INFORMATION);
                    refreshCoursesTable();
                } else {
                    showAlert("Error", "Failed to update course", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void deleteCourse(Course course) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete course: " + course.getTitle());
        confirmation.setContentText("Are you sure? This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (CourseManager.deleteCourse(course.getId())) {
                    showAlert("Success", "Course deleted successfully!", Alert.AlertType.INFORMATION);
                    refreshCoursesTable();
                } else {
                    showAlert("Error", "Failed to delete course", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private Tab createNotesTab() {
        Tab tab = new Tab("Course Notes");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        HBox toolbar = new HBox(10);
        Button uploadNoteBtn = new Button("Upload Note");
        uploadNoteBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        uploadNoteBtn.setOnAction(e -> showUploadNoteDialog());

        Button undoBtn = new Button("Undo Last Upload");
        undoBtn.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white;");
        undoBtn.setOnAction(e -> undoLastUpload());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshNotesTable());

        toolbar.getChildren().addAll(uploadNoteBtn, undoBtn, refreshBtn);

        notesTable = new TableView<>();

        TableColumn<Note, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Note, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(250);

        TableColumn<Note, Integer> courseCol = new TableColumn<>("Course ID");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        courseCol.setPrefWidth(100);

        TableColumn<Note, Boolean> freeCol = new TableColumn<>("Access");
        freeCol.setCellValueFactory(new PropertyValueFactory<>("free"));
        freeCol.setCellFactory(col -> new TableCell<Note, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Free" : "Premium");
                }
            }
        });
        freeCol.setPrefWidth(80);

        TableColumn<Note, String> fileCol = new TableColumn<>("File Path");
        fileCol.setCellValueFactory(new PropertyValueFactory<>("filePath"));
        fileCol.setPrefWidth(300);

        notesTable.getColumns().addAll(idCol, titleCol, courseCol, freeCol, fileCol);

        refreshNotesTable();

        content.getChildren().addAll(toolbar, notesTable);
        tab.setContent(content);
        return tab;
    }

    private void refreshNotesTable() {
        List<Note> notes = NoteManager.getNotesByTeacher(teacher.getId());
        notesTable.setItems(FXCollections.observableArrayList(notes));
    }

    private void showUploadNoteDialog() {
        List<Course> courses = CourseManager.getCoursesByTeacher(teacher.getId());
        if (courses.isEmpty()) {
            showAlert("No Courses", "You need to create a course first", Alert.AlertType.WARNING);
            return;
        }

        Dialog<Note> dialog = new Dialog<>();
        dialog.setTitle("Upload Note");
        dialog.setHeaderText("Upload course materials");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<Course> courseCombo = new ComboBox<>();
        courseCombo.setItems(FXCollections.observableArrayList(courses));
        courseCombo.setCellFactory(param -> new ListCell<Course>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });
        courseCombo.setButtonCell(new ListCell<Course>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });

        TextField titleField = new TextField();
        titleField.setPromptText("Note Title");

        TextField filePathField = new TextField();
        filePathField.setPromptText("File Path");
        filePathField.setEditable(false);

        Button browseBtn = new Button("Browse...");
        browseBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Note File");
            File selectedFile = fileChooser.showOpenDialog(dialog.getOwner());
            if (selectedFile != null) {
                filePathField.setText(selectedFile.getAbsolutePath());
            }
        });

        CheckBox freeCheckBox = new CheckBox("Free Access");
        freeCheckBox.setSelected(true);

        grid.add(new Label("Course:"), 0, 0);
        grid.add(courseCombo, 1, 0);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("File:"), 0, 2);
        HBox fileBox = new HBox(5, filePathField, browseBtn);
        grid.add(fileBox, 1, 2);
        grid.add(freeCheckBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType uploadButtonType = new ButtonType("Upload", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(uploadButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == uploadButtonType) {
                Course selectedCourse = courseCombo.getValue();
                String title = titleField.getText().trim();
                String filePath = filePathField.getText().trim();
                boolean isFree = freeCheckBox.isSelected();

                if (selectedCourse == null || title.isEmpty()) {
                    showAlert("Error", "Please fill all required fields", Alert.AlertType.ERROR);
                    return null;
                }

                String storedPath = filePath;
                if (!filePath.isEmpty()) {
                    File sourceFile = new File(filePath);
                    String fileName = System.currentTimeMillis() + "_" + sourceFile.getName();
                    storedPath = FileHandler.copyNoteFile(sourceFile, fileName);
                    if (storedPath == null) {
                        showAlert("Error", "Failed to copy file", Alert.AlertType.ERROR);
                        return null;
                    }
                }

                Note note = new Note(selectedCourse.getId(), teacher.getId(), title, storedPath, isFree);
                int noteId = NoteManager.uploadNote(note);

                if (noteId > 0) {
                    showAlert("Success", "Note uploaded successfully! Stack size: " + NoteManager.getUndoStackSize(), Alert.AlertType.INFORMATION);
                    refreshNotesTable();
                    return note;
                } else {
                    showAlert("Error", "Failed to upload note", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void undoLastUpload() {
        if (NoteManager.undoLastUpload(teacher.getId())) {
            showAlert("Success", "Last note upload undone successfully!", Alert.AlertType.INFORMATION);
            refreshNotesTable();
        } else {
            showAlert("Error", "No notes to undo or operation failed", Alert.AlertType.WARNING);
        }
    }

    private Tab createStatisticsTab() {
        Tab tab = new Tab("Statistics");
        tab.setClosable(false);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label label = new Label("Teaching Statistics");
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Map<String, Object> stats = ChartDataProvider.getTeacherStatistics(teacher.getId());

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 5;");

        grid.add(new Label("Total Courses:"), 0, 0);
        grid.add(new Label(stats.getOrDefault("totalCourses", 0).toString()), 1, 0);

        grid.add(new Label("Total Enrollments:"), 0, 1);
        grid.add(new Label(stats.getOrDefault("totalEnrollments", 0).toString()), 1, 1);

        grid.add(new Label("Total Notes:"), 0, 2);
        grid.add(new Label(stats.getOrDefault("totalNotes", 0).toString()), 1, 2);

        content.getChildren().addAll(label, grid);
        tab.setContent(content);
        return tab;
    }

    private Tab createProfileTab() {
        Tab tab = new Tab("Profile");
        tab.setClosable(false);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label label = new Label("Profile Information");
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(new Label(teacher.getName()), 1, 0);

        grid.add(new Label("Email:"), 0, 1);
        grid.add(new Label(teacher.getEmail()), 1, 1);

        grid.add(new Label("Role:"), 0, 2);
        grid.add(new Label(teacher.getRole().toString()), 1, 2);

        content.getChildren().addAll(label, grid);
        tab.setContent(content);
        return tab;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
