package ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import manager.*;
import model.*;

import java.util.List;
import java.util.Map;


 // Admin Dashboard UI

public class AdminPane extends VBox {

    private User admin;
    private MainApp mainApp;
    private TableView<Payment> paymentsTable;

    public AdminPane(User admin, MainApp mainApp) {
        this.admin = admin;
        this.mainApp = mainApp;

        setPadding(new Insets(20));
        setSpacing(15);

        // Header
        HBox header = createHeader();

        // Tab pane
        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
            createDashboardTab(),
            createPaymentApprovalTab(),
            createAnalyticsTab(),
            createManagementTab()
        );

        getChildren().addAll(header, tabPane);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #9C27B0; -fx-background-radius: 5;");

        Label welcomeLabel = new Label("Admin Dashboard - " + admin.getName());
        welcomeLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: white; -fx-text-fill: #9C27B0;");
        logoutButton.setOnAction(e -> mainApp.logout());

        header.getChildren().addAll(welcomeLabel, spacer, logoutButton);
        return header;
    }

    private Tab createDashboardTab() {
        Tab tab = new Tab("Dashboard");
        tab.setClosable(false);

        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        Label label = new Label("System Overview");
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Statistics cards
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(15);

        Map<String, Object> stats = ChartDataProvider.getTotalStatistics();

        statsGrid.add(createStatCard("Total Users", stats.getOrDefault("totalUsers", 0).toString(), "#2196F3"), 0, 0);
        statsGrid.add(createStatCard("Total Courses", stats.getOrDefault("totalCourses", 0).toString(), "#4CAF50"), 1, 0);
        statsGrid.add(createStatCard("Total Enrollments", stats.getOrDefault("totalEnrollments", 0).toString(), "#FF9800"), 2, 0);
        statsGrid.add(createStatCard("Total Revenue", "$" + stats.getOrDefault("totalRevenue", 0.0).toString(), "#9C27B0"), 0, 1);
        statsGrid.add(createStatCard("Pending Payments", stats.getOrDefault("pendingPayments", 0).toString(), "#F44336"), 1, 1);

        content.getChildren().addAll(label, statsGrid);
        tab.setContent(content);
        return tab;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 5;");
        card.setPrefWidth(200);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private Tab createPaymentApprovalTab() {
        Tab tab = new Tab("Payment Approval");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        HBox toolbar = new HBox(10);
        Button approveNextBtn = new Button("Approve Next (Queue)");
        approveNextBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        approveNextBtn.setOnAction(e -> approveNextPayment());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshPaymentsTable());

        Label queueLabel = new Label("Queue Size: " + PaymentManager.getQueueSize());
        queueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        toolbar.getChildren().addAll(approveNextBtn, refreshBtn, queueLabel);

        // Payments table
        paymentsTable = new TableView<>();

        TableColumn<Payment, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Payment, Integer> studentCol = new TableColumn<>("Student ID");
        studentCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        studentCol.setPrefWidth(100);

        TableColumn<Payment, Integer> courseCol = new TableColumn<>("Course ID");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        courseCol.setPrefWidth(100);

        TableColumn<Payment, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(100);

        TableColumn<Payment, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        TableColumn<Payment, String> timeCol = new TableColumn<>("Request Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeCol.setPrefWidth(200);

        TableColumn<Payment, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(200);
        actionCol.setCellFactory(col -> new TableCell<Payment, Void>() {
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");

            {
                approveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                approveBtn.setOnAction(e -> {
                    Payment payment = getTableView().getItems().get(getIndex());
                    approvePayment(payment);
                });

                rejectBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                rejectBtn.setOnAction(e -> {
                    Payment payment = getTableView().getItems().get(getIndex());
                    rejectPayment(payment);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Payment payment = getTableView().getItems().get(getIndex());
                    if (payment.getStatus() == Payment.PaymentStatus.PENDING) {
                        HBox buttons = new HBox(5, approveBtn, rejectBtn);
                        setGraphic(buttons);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        paymentsTable.getColumns().addAll(idCol, studentCol, courseCol, amountCol, statusCol, timeCol, actionCol);

        refreshPaymentsTable();

        content.getChildren().addAll(toolbar, paymentsTable);
        tab.setContent(content);
        return tab;
    }

    private void approveNextPayment() {
        if (PaymentManager.approveNextPayment()) {
            showAlert("Success", "Payment approved and student enrolled!", Alert.AlertType.INFORMATION);
            refreshPaymentsTable();
        } else {
            showAlert("Error", "No pending payments or approval failed", Alert.AlertType.WARNING);
        }
    }

    private void approvePayment(Payment payment) {
        if (PaymentManager.approvePayment(payment.getId())) {
            showAlert("Success", "Payment #" + payment.getId() + " approved!", Alert.AlertType.INFORMATION);
            refreshPaymentsTable();
        } else {
            showAlert("Error", "Failed to approve payment", Alert.AlertType.ERROR);
        }
    }

    private void rejectPayment(Payment payment) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Rejection");
        confirmation.setHeaderText("Reject payment #" + payment.getId());
        confirmation.setContentText("Are you sure you want to reject this payment?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (PaymentManager.rejectPayment(payment.getId())) {
                    showAlert("Success", "Payment rejected", Alert.AlertType.INFORMATION);
                    refreshPaymentsTable();
                } else {
                    showAlert("Error", "Failed to reject payment", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void refreshPaymentsTable() {
        List<Payment> payments = PaymentManager.getAllPayments();
        paymentsTable.setItems(FXCollections.observableArrayList(payments));
    }

    private Tab createAnalyticsTab() {
        Tab tab = new Tab("Analytics");
        tab.setClosable(false);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);

        VBox content = new VBox(20);
        content.setPadding(new Insets(15));

        Label label = new Label("System Analytics");
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Enrollment Chart
        BarChart<String, Number> enrollmentChart = createEnrollmentChart();

        // Revenue Chart
        BarChart<String, Number> revenueChart = createRevenueChart();

        // Payment Status Chart
        PieChart paymentStatusChart = createPaymentStatusChart();

        // User Role Chart
        PieChart userRoleChart = createUserRoleChart();

        GridPane chartsGrid = new GridPane();
        chartsGrid.setHgap(15);
        chartsGrid.setVgap(15);
        chartsGrid.add(enrollmentChart, 0, 0);
        chartsGrid.add(revenueChart, 1, 0);
        chartsGrid.add(paymentStatusChart, 0, 1);
        chartsGrid.add(userRoleChart, 1, 1);

        content.getChildren().addAll(label, chartsGrid);
        scrollPane.setContent(content);

        tab.setContent(scrollPane);
        return tab;
    }

    private BarChart<String, Number> createEnrollmentChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Top Courses by Enrollment");
        chart.setPrefSize(500, 300);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Enrollments");

        Map<String, Integer> stats = ChartDataProvider.getEnrollmentStatistics();
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        chart.getData().add(series);
        return chart;
    }

    private BarChart<String, Number> createRevenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Revenue by Course");
        chart.setPrefSize(500, 300);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue ($)");

        Map<String, Double> stats = ChartDataProvider.getRevenueStatistics();
        for (Map.Entry<String, Double> entry : stats.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        chart.getData().add(series);
        return chart;
    }

    private PieChart createPaymentStatusChart() {
        PieChart chart = new PieChart();
        chart.setTitle("Payment Status Distribution");
        chart.setPrefSize(500, 300);

        Map<String, Integer> stats = ChartDataProvider.getPaymentStatusDistribution();
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            chart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        return chart;
    }

    private PieChart createUserRoleChart() {
        PieChart chart = new PieChart();
        chart.setTitle("User Role Distribution");
        chart.setPrefSize(500, 300);

        Map<String, Integer> stats = ChartDataProvider.getUserRoleDistribution();
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            chart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        return chart;
    }

    private Tab createManagementTab() {
        Tab tab = new Tab("Management");
        tab.setClosable(false);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label label = new Label("System Management");
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(15);
        buttonGrid.setVgap(15);

        Button viewUsersBtn = new Button("View All Users");
        viewUsersBtn.setPrefWidth(200);
        viewUsersBtn.setOnAction(e -> showAlert("Feature", "User management feature", Alert.AlertType.INFORMATION));

        Button viewCoursesBtn = new Button("View All Courses");
        viewCoursesBtn.setPrefWidth(200);
        viewCoursesBtn.setOnAction(e -> showAllCourses());

        Button rebuildGraphBtn = new Button("Rebuild Recommendation Graph");
        rebuildGraphBtn.setPrefWidth(200);
        rebuildGraphBtn.setOnAction(e -> {
            RecommendationEngine.rebuildGraph();
            showAlert("Success", "Recommendation graph rebuilt", Alert.AlertType.INFORMATION);
        });

        Button databaseStatsBtn = new Button("Database Statistics");
        databaseStatsBtn.setPrefWidth(200);
        databaseStatsBtn.setOnAction(e -> showDatabaseStats());

        buttonGrid.add(viewUsersBtn, 0, 0);
        buttonGrid.add(viewCoursesBtn, 1, 0);
        buttonGrid.add(rebuildGraphBtn, 0, 1);
        buttonGrid.add(databaseStatsBtn, 1, 1);

        content.getChildren().addAll(label, buttonGrid);
        tab.setContent(content);
        return tab;
    }

    private void showAllCourses() {
        List<Course> courses = CourseManager.getAllCourses();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("All Courses");
        dialog.setHeaderText("System Courses (" + courses.size() + ")");

        TableView<Course> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(courses));

        TableColumn<Course, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(250);

        TableColumn<Course, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Course, Integer> enrolledCol = new TableColumn<>("Enrolled");
        enrolledCol.setCellValueFactory(new PropertyValueFactory<>("enrolledCount"));

        table.getColumns().addAll(titleCol, priceCol, enrolledCol);
        table.setPrefHeight(400);

        dialog.getDialogPane().setContent(table);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showDatabaseStats() {
        Map<String, Object> stats = ChartDataProvider.getTotalStatistics();

        StringBuilder sb = new StringBuilder();
        sb.append("Database Statistics:\n\n");
        sb.append("Total Users: ").append(stats.get("totalUsers")).append("\n");
        sb.append("Total Courses: ").append(stats.get("totalCourses")).append("\n");
        sb.append("Total Enrollments: ").append(stats.get("totalEnrollments")).append("\n");
        sb.append("Total Revenue: $").append(stats.get("totalRevenue")).append("\n");
        sb.append("Pending Payments: ").append(stats.get("pendingPayments")).append("\n");
        sb.append("\nQueue Status:\n");
        sb.append("Payment Queue Size: ").append(PaymentManager.getQueueSize()).append("\n");
        sb.append("Undo Stack Size: ").append(NoteManager.getUndoStackSize()).append("\n");

        showAlert("Database Statistics", sb.toString(), Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
