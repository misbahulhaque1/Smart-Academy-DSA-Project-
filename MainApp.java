package ui;
import javafx.collections.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import manager.UserManager;
import manager.RecommendationEngine;
import model.Role;
import model.User;
import persistence.DBConnection;
import util.HashUtil;


//  Main Application Entry point

public class MainApp extends Application {

    private Stage primaryStage;
    private User currentUser;


    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("SmartAcademy - Learning Management System");

        // Test database connection
        if (!DBConnection.testConnection()) {
            showAlert("Database Error", "Cannot connect to database. Please check your configuration.", Alert.AlertType.ERROR);
            return;
        }

        // Initialize recommendation graph
        RecommendationEngine.buildGraphFromDB();

        // Show login screen
        showLoginScreen();

        primaryStage.show();
    }
//show login
    private void showLoginScreen() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f5f5f5;");

        // Title
        Label titleLabel = new Label("SmartAcademy");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        Label subtitleLabel = new Label("Learning Management System");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");

        // Login form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(15);
        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(400);
        form.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 10;");

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setPrefWidth(300);

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefWidth(300);

        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(150);
        loginButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px;");

        Button registerButton = new Button("Register");
        registerButton.setPrefWidth(150);
        registerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");

        // Sample credentials info
        Label infoLabel = new Label("Sample Login:\nStudent: student@smartacademy.com\nTeacher: teacher@smartacademy.com\nAdmin: admin@smartacademy.com\nPassword: 12345678");
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888; -fx-text-alignment: center;");

        form.add(emailLabel, 0, 0);
        form.add(emailField, 0, 1);
        form.add(passwordLabel, 0, 2);
        form.add(passwordField, 0, 3);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(loginButton, registerButton);

        root.getChildren().addAll(titleLabel, subtitleLabel, form, buttonBox, infoLabel);

        // Login action
        loginButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText();

            if (email.isEmpty() || password.isEmpty()) {
                showAlert("Login Error", "Please enter email and password", Alert.AlertType.WARNING);
                return;
            }

            User user = UserManager.login(email, password);
            if (user != null) {
                currentUser = user;
                showDashboard();
            } else {
                showAlert("Login Failed", "Invalid email or password", Alert.AlertType.ERROR);
            }
        });

        // Register action
        registerButton.setOnAction(e -> showRegisterDialog());

        // Enter key to login
        passwordField.setOnAction(e -> loginButton.fire());

        Scene scene = new Scene(root, 600, 600);
        primaryStage.setScene(scene);
    }

    /**
     * Show registration dialog
     */
    private void showRegisterDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Register New User");
        dialog.setHeaderText("Create a new account");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");

        ComboBox<Role> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(Role.STUDENT, Role.TEACHER);
        roleCombo.setValue(Role.STUDENT);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Confirm:"), 0, 3);
        grid.add(confirmPasswordField, 1, 3);
        grid.add(new Label("Role:"), 0, 4);
        grid.add(roleCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String password = passwordField.getText();
                String confirmPassword = confirmPasswordField.getText();
                Role role = roleCombo.getValue();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    showAlert("Registration Error", "Please fill all fields", Alert.AlertType.WARNING);
                    return null;
                }

                if (!password.equals(confirmPassword)) {
                    showAlert("Registration Error", "Passwords do not match", Alert.AlertType.WARNING);
                    return null;
                }

                if (password.length() < 6) {
                    showAlert("Registration Error", "Password must be at least 6 characters", Alert.AlertType.WARNING);
                    return null;
                }

                User user = new User(name, email, HashUtil.hashPassword(password), role);

                if (UserManager.register(user)) {
                    showAlert("Registration Successful", "Account created! Please login.", Alert.AlertType.INFORMATION);
                    return user;
                } else {
                    showAlert("Registration Failed", "Email already exists or database error", Alert.AlertType.ERROR);
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Show dashboard based on user role
     */
    private void showDashboard() {
        BorderPane root = new BorderPane();

        if (currentUser.getRole() == Role.STUDENT) {
            root.setCenter(new StudentPane(currentUser, this));
        } else if (currentUser.getRole() == Role.TEACHER) {
            root.setCenter(new TeacherPane(currentUser, this));
        } else if (currentUser.getRole() == Role.ADMIN) {
            root.setCenter(new AdminPane(currentUser, this));
        }

        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("SmartAcademy - " + currentUser.getName() + " (" + currentUser.getRole() + ")");
    }

    /**
     * Logout and return to login screen
     */
    public void logout() {
        currentUser = null;
        showLoginScreen();
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
