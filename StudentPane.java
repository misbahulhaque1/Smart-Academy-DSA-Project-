package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import manager.*;
import model.*;

import java.util.List;
import java.util.Map;

public class StudentPane extends VBox {

    private User student;
    private MainApp mainApp;
    private TableView<Course> coursesTable;
    private TableView<Course> enrolledTable;
    private TableView<Payment> paymentsTable;
    private ListView<String> recommendationsListView;

    public StudentPane(User student, MainApp mainApp) {
        this.student = student;
        this.mainApp = mainApp;

        setPadding(new Insets(20));
        setSpacing(15);

        HBox header = createHeader();

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                createBrowseCoursesTab(),
                createMyCoursesTab(),
                createPaymentsTab(),
                createRecommendationsTab(),
                createProfileTab()
        );

        getChildren().addAll(header, tabPane);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #2196F3; -fx-background-radius: 5;");

        Label welcomeLabel = new Label("Welcome, " + student.getName());
        welcomeLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: white; -fx-text-fill: #2196F3;");
        logoutButton.setOnAction(e -> mainApp.logout());

        header.getChildren().addAll(welcomeLabel, spacer, logoutButton);
        return header;
    }

    private Tab createBrowseCoursesTab() {
        Tab tab = new Tab("Browse Courses");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        // search bar
        HBox searchBar = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("Search courses...");
        searchField.setPrefWidth(300);

        Button searchButton = new Button("Search");
        Button showAllButton = new Button("Show All");

        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All Courses", "Free Courses", "Paid Courses");
        filterCombo.setValue("All Courses");

        searchBar.getChildren().addAll(searchField, searchButton, showAllButton, filterCombo);

        // table
        coursesTable = new TableView<>();

        TableColumn<Course, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Course, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(250);

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
        paidCol.setPrefWidth(80);

        TableColumn<Course, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(80);

        TableColumn<Course, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(col -> new TableCell<Course, Void>() {
            private final Button enrollBtn = new Button("Enroll");
            {
                enrollBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                enrollBtn.setOnAction(e -> {
                    Course course = getTableView().getItems().get(getIndex());
                    enrollInCourse(course);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : enrollBtn);
            }
        });

        coursesTable.getColumns().addAll(idCol, titleCol, descCol, paidCol, priceCol, actionCol);

        loadAllCourses();

        // search actions
        searchButton.setOnAction(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                List<Course> results = CourseManager.searchByPartialTitle(query);
                coursesTable.setItems(FXCollections.observableArrayList(results));
            }
        });

        showAllButton.setOnAction(e -> loadAllCourses());

        filterCombo.setOnAction(e -> {
            String filter = filterCombo.getValue();
            if ("Free Courses".equals(filter)) {
                coursesTable.setItems(FXCollections.observableArrayList(CourseManager.getCoursesByType(false)));
            } else if ("Paid Courses".equals(filter)) {
                coursesTable.setItems(FXCollections.observableArrayList(CourseManager.getCoursesByType(true)));
            } else {
                loadAllCourses();
            }
        });

        content.getChildren().addAll(searchBar, coursesTable);
        tab.setContent(content);
        return tab;
    }

    private void loadAllCourses() {
        List<Course> courses = CourseManager.getAllCourses();
        coursesTable.setItems(FXCollections.observableArrayList(courses));
    }

    private void enrollInCourse(Course course) {
        if (PaymentManager.isEnrolled(student.getId(), course.getId())) {
            showAlert("Already Enrolled", "You are already enrolled in this course.", Alert.AlertType.INFORMATION);
            return;
        }

        if (course.isPaid()) {
            Payment payment = new Payment(student.getId(), course.getId(), course.getPrice());
            int paymentId = PaymentManager.requestPayment(payment);
            if (paymentId > 0) {
                showAlert("Payment Requested", "Payment request submitted. Awaiting admin approval.", Alert.AlertType.INFORMATION);
                refreshPaymentsTable();
            } else {
                showAlert("Error", "Failed to create payment request.", Alert.AlertType.ERROR);
            }
        } else {
            boolean ok = PaymentManager.enrollStudentDirectly(student.getId(), course.getId());
            if (ok) {
                showAlert("Success", "Successfully enrolled in " + course.getTitle(), Alert.AlertType.INFORMATION);
                refreshEnrolledTable();
            } else {
                showAlert("Error", "Failed to enroll in course.", Alert.AlertType.ERROR);
            }
        }
    }

    private Tab createMyCoursesTab() {
        Tab tab = new Tab("My Courses");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label label = new Label("Your Enrolled Courses");
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        enrolledTable = new TableView<>();

        TableColumn<Course, String> titleCol = new TableColumn<>("Course Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(300);

        TableColumn<Course, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(400);

        TableColumn<Course, Void> notesCol = new TableColumn<>("Notes");
        notesCol.setPrefWidth(100);
        notesCol.setCellFactory(col -> new TableCell<Course, Void>() {
            private final Button viewBtn = new Button("View Notes");
            {
                viewBtn.setOnAction(e -> {
                    Course course = getTableView().getItems().get(getIndex());
                    viewCourseNotes(course);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewBtn);
            }
        });

        enrolledTable.getColumns().addAll(titleCol, descCol, notesCol);

        refreshEnrolledTable();

        content.getChildren().addAll(label, enrolledTable);
        tab.setContent(content);
        return tab;
    }

    private void refreshEnrolledTable() {
        List<Course> enrolledCourses = CourseManager.getEnrolledCourses(student.getId());
        enrolledTable.setItems(FXCollections.observableArrayList(enrolledCourses));
    }

    private void viewCourseNotes(Course course) {
        List<Note> notes = NoteManager.getNotesByCourse(course.getId());

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Course Notes - " + course.getTitle());
        dialog.setHeaderText("Available Notes");

        ListView<String> notesList = new ListView<>();
        ObservableList<String> noteItems = FXCollections.observableArrayList();

        for (Note note : notes) {
            if (NoteManager.canAccessNote(student.getId(), note.getId())) {
                String noteText = note.getTitle() + (note.isFree() ? " [FREE]" : " [PREMIUM]");
                noteItems.add(noteText);
            }
        }

        notesList.setItems(noteItems);
        notesList.setPrefHeight(300);

        dialog.getDialogPane().setContent(notesList);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private Tab createPaymentsTab() {
        Tab tab = new Tab("Payments");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label label = new Label("Payment History");
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        paymentsTable = new TableView<>();

        TableColumn<Payment, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Payment, Integer> courseCol = new TableColumn<>("Course ID");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("courseId"));

        TableColumn<Payment, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<Payment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        paymentsTable.getColumns().addAll(idCol, courseCol, amountCol, statusCol);

        refreshPaymentsTable();

        content.getChildren().addAll(label, paymentsTable);
        tab.setContent(content);
        return tab;
    }

    private void refreshPaymentsTable() {
        List<Payment> payments = PaymentManager.getPaymentsByStudent(student.getId());
        paymentsTable.setItems(FXCollections.observableArrayList(payments));
    }

    private Tab createRecommendationsTab() {
        Tab tab = new Tab("Recommendations");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label label = new Label("Recommended Courses For You");
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        recommendationsListView = new ListView<>();

        List<Course> recommended = RecommendationEngine.recommendForStudent(student.getId(), 10);
        ObservableList<String> items = FXCollections.observableArrayList();
        for (Course course : recommended) {
            items.add(course.getTitle() + " - $" + course.getPrice() + (course.isPaid() ? " (Paid)" : " (Free)"));
        }
        recommendationsListView.setItems(items);

        content.getChildren().addAll(label, recommendationsListView);
        tab.setContent(content);
        return tab;
    }

    private Tab createProfileTab() {
        Tab tab = new Tab("Profile");
        tab.setClosable(false);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label label = new Label("Profile & Statistics");
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(new Label(student.getName()), 1, 0);

        grid.add(new Label("Email:"), 0, 1);
        grid.add(new Label(student.getEmail()), 1, 1);

        grid.add(new Label("Role:"), 0, 2);
        grid.add(new Label(student.getRole().toString()), 1, 2);

        Map<String, Object> stats = ChartDataProvider.getStudentStatistics(student.getId());

        grid.add(new Label("Enrolled Courses:"), 0, 3);
        grid.add(new Label(stats.getOrDefault("enrolledCourses", 0).toString()), 1, 3);

        grid.add(new Label("Total Paid:"), 0, 4);
        grid.add(new Label("$" + stats.getOrDefault("totalPaid", 0.0).toString()), 1, 4);

        grid.add(new Label("Pending Payments:"), 0, 5);
        grid.add(new Label(stats.getOrDefault("pendingPayments", 0).toString()), 1, 5);

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
