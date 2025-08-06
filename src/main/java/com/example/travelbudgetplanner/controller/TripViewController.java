// Step 3: Updated TripViewController with storage integration

package com.example.travelbudgetplanner.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;

// Import your new classes
import com.example.travelbudgetplanner.model.TripData;
import com.example.travelbudgetplanner.model.Expense;
import com.example.travelbudgetplanner.service.TripDataStorageService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;

public class TripViewController {

    // --- Trip Details Tab ---
    @FXML private TextField destinationField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField budgetField;
    @FXML private Button saveTripButton;
    @FXML private Button clearTripButton;
    @FXML private Button loadTripButton; // New button

    // --- Expense Tracker Tab ---
    @FXML private TableView<ExpenseRow> expenseTable;
    @FXML private TableColumn<ExpenseRow, String> categoryColumn;
    @FXML private TableColumn<ExpenseRow, Number> amountColumn;
    @FXML private TableColumn<ExpenseRow, String> dateColumn;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField amountField;
    @FXML private DatePicker expenseDatePicker;
    @FXML private Button addExpenseButton;
    @FXML private Button deleteExpenseButton;

    // --- Budget Summary Tab ---
    @FXML private Label totalBudgetLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label remainingBudgetLabel;
    @FXML private Label statusLabel;

    // --- Reports Tab ---
    @FXML private Button generateReportsButton;
    @FXML private Button categoryReportButton;
    @FXML private Button dailyReportButton;
    @FXML private Button budgetComparisonButton;

    // Data model and storage
    private final ObservableList<ExpenseRow> expenses = FXCollections.observableArrayList();
    private TripData currentTrip;
    private final TripDataStorageService storageService;

    // Constructor
    public TripViewController() {
        this.storageService = new TripDataStorageService();
        this.currentTrip = new TripData();
    }

    @FXML
    private void initialize() {
        // Setup table columns
        categoryColumn.setCellValueFactory(data -> data.getValue().categoryProperty());
        amountColumn.setCellValueFactory(data -> data.getValue().amountProperty());
        dateColumn.setCellValueFactory(data -> data.getValue().dateProperty());

        expenseTable.setItems(expenses);

        // Populate category options
        categoryCombo.setItems(FXCollections.observableArrayList(
                "Transport", "Food", "Lodging", "Activities", "Other"
        ));

        // Wire buttons
        saveTripButton.setOnAction(e -> saveTrip());
        clearTripButton.setOnAction(e -> clearTripForm());
        addExpenseButton.setOnAction(e -> addExpense());
        deleteExpenseButton.setOnAction(e -> deleteSelectedExpense());

        // New load button
        if (loadTripButton != null) {
            loadTripButton.setOnAction(e -> loadCurrentTrip());
        }

        // Wire report buttons
        if (generateReportsButton != null) {
            generateReportsButton.setOnAction(e -> showFullReportWindow());
        }
        if (categoryReportButton != null) {
            categoryReportButton.setOnAction(e -> showCategoryReport());
        }
        if (dailyReportButton != null) {
            dailyReportButton.setOnAction(e -> showDailyReport());
        }
        if (budgetComparisonButton != null) {
            budgetComparisonButton.setOnAction(e -> showBudgetComparison());
        }

        // Load existing trip data when the application starts
        loadCurrentTrip();
    }

    private void saveTrip() {
        try {
            // Update current trip data
            currentTrip.setDestination(destinationField.getText().trim());
            currentTrip.setStartDate(startDatePicker.getValue());
            currentTrip.setEndDate(endDatePicker.getValue());

            try {
                double budget = Double.parseDouble(budgetField.getText().trim());
                currentTrip.setBudget(budget);
            } catch (NumberFormatException ex) {
                currentTrip.setBudget(0);
            }

            // Convert ExpenseRow to Expense and update trip data
            List<com.example.travelbudgetplanner.model.Expense> expenseList = new ArrayList<>();
            for (ExpenseRow row : expenses) {
                LocalDate expenseDate = LocalDate.parse(row.getDate());
                com.example.travelbudgetplanner.model.Expense expense =
                        new com.example.travelbudgetplanner.model.Expense(
                                row.getCategory(), row.getAmount(), expenseDate);
                expenseList.add(expense);
            }
            currentTrip.setExpenses(expenseList);

            // Save to storage
            storageService.saveCurrentTrip(currentTrip);

            // Show success message
            showAlert("Success", "Trip data saved successfully!");

            updateSummary();

        } catch (IOException e) {
            showAlert("Error", "Failed to save trip data: " + e.getMessage());
        }
    }

    private void loadCurrentTrip() {
        try {
            if (storageService.currentTripExists()) {
                currentTrip = storageService.loadCurrentTrip();

                // Update UI with loaded data
                destinationField.setText(currentTrip.getDestination() != null ? currentTrip.getDestination() : "");
                startDatePicker.setValue(currentTrip.getStartDate());
                endDatePicker.setValue(currentTrip.getEndDate());
                budgetField.setText(currentTrip.getBudget() > 0 ? String.valueOf(currentTrip.getBudget()) : "");

                // Load expenses
                expenses.clear();
                for (com.example.travelbudgetplanner.model.Expense expense : currentTrip.getExpenses()) {
                    expenses.add(new ExpenseRow(
                            expense.getCategory(),
                            expense.getAmount(),
                            expense.getDate().toString()
                    ));
                }

                updateSummary();
                showAlert("Success", "Trip data loaded successfully!");
            }
        } catch (IOException e) {
            showAlert("Error", "Failed to load trip data: " + e.getMessage());
        }
    }

    private void clearTripForm() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Clear Trip Data");
        confirmation.setHeaderText("Are you sure?");
        confirmation.setContentText("This will clear all trip data and expenses. This action cannot be undone.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Clear UI
            destinationField.clear();
            startDatePicker.setValue(null);
            endDatePicker.setValue(null);
            budgetField.clear();
            expenses.clear();

            // Clear current trip data
            currentTrip = new TripData();

            // Clear from storage
            storageService.clearCurrentTrip();

            updateSummary();
        }
    }

    private void addExpense() {
        String cat = categoryCombo.getValue();
        String amtText = amountField.getText();
        LocalDate date = expenseDatePicker.getValue() == null ?
                LocalDate.now() : expenseDatePicker.getValue();

        double amt = 0;
        try {
            amt = Double.parseDouble(amtText.trim());
        } catch (Exception ignore) {}

        if (cat != null && amt > 0) {
            // Add to UI table
            expenses.add(new ExpenseRow(cat, amt, date.toString()));

            // Clear input fields
            amountField.clear();
            expenseDatePicker.setValue(null);
            categoryCombo.setValue(null);

            // Auto-save after adding expense
            saveTrip();

            updateSummary();
        }
    }

    private void deleteSelectedExpense() {
        ExpenseRow selected = expenseTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Delete Expense");
            confirmation.setHeaderText("Delete this expense?");
            confirmation.setContentText(String.format("Category: %s\nAmount: $%.2f\nDate: %s",
                    selected.getCategory(), selected.getAmount(), selected.getDate()));

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                expenses.remove(selected);

                // Auto-save after deleting expense
                saveTrip();

                updateSummary();
            }
        }
    }

    private void updateSummary() {
        double totalExpenses = expenses.stream().mapToDouble(ExpenseRow::getAmount).sum();
        double tripBudget = currentTrip.getBudget();
        double remaining = tripBudget - totalExpenses;

        totalBudgetLabel.setText(String.format("$%.2f", tripBudget));
        totalExpensesLabel.setText(String.format("$%.2f", totalExpenses));
        remainingBudgetLabel.setText(String.format("$%.2f", remaining));

        if (tripBudget <= 0) {
            statusLabel.setText("No budget set.");
            statusLabel.setStyle("-fx-text-fill: gray;");
        } else if (remaining < 0) {
            statusLabel.setText("Over budget!");
            statusLabel.setStyle("-fx-text-fill: red;");
        } else if (remaining < tripBudget * 0.1) {
            statusLabel.setText("Warning: Nearly out of budget.");
            statusLabel.setStyle("-fx-text-fill: orange;");
        } else {
            statusLabel.setText("On track.");
            statusLabel.setStyle("-fx-text-fill: green;");
        }
    }

    // Add method to save trip to history (for completed trips)
    private void saveTripToHistory() {
        if (currentTrip.getDestination() != null && !currentTrip.getDestination().isEmpty()) {
            try {
                storageService.saveTripToHistory(currentTrip);
                showAlert("Success", "Trip saved to history!");
            } catch (IOException e) {
                showAlert("Error", "Failed to save trip to history: " + e.getMessage());
            }
        }
    }

    // === REPORTING METHODS ===

    /**
     * Shows a comprehensive report window with all charts
     */
    private void showFullReportWindow() {
        if (expenses.isEmpty()) {
            showAlert("No Data", "No expenses recorded yet. Add some expenses first.");
            return;
        }

        Stage reportStage = new Stage();
        reportStage.initModality(Modality.APPLICATION_MODAL);
        reportStage.setTitle("Travel Budget Reports - " +
                (destinationField.getText().isEmpty() ? "Unknown Destination" : destinationField.getText()));

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));

        // Title
        Label titleLabel = new Label("Expense Reports");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Create charts in horizontal layout
        HBox chartsLayout = new HBox(20);

        // Category breakdown (pie chart)
        PieChart categoryChart = createCategoryPieChart();
        categoryChart.setPrefSize(300, 300);

        VBox categorySection = new VBox(5);
        Label categoryTitle = new Label("Expenses by Category");
        categoryTitle.setStyle("-fx-font-weight: bold;");
        categorySection.getChildren().addAll(categoryTitle, categoryChart);

        // Daily spending (bar chart)
        BarChart<String, Number> dailyChart = createDailyBarChart();
        dailyChart.setPrefSize(400, 300);

        VBox dailySection = new VBox(5);
        Label dailyTitle = new Label("Daily Spending");
        dailyTitle.setStyle("-fx-font-weight: bold;");
        dailySection.getChildren().addAll(dailyTitle, dailyChart);

        chartsLayout.getChildren().addAll(categorySection, dailySection);

        // Budget comparison chart
        BarChart<String, Number> budgetChart = createBudgetComparisonChart();
        budgetChart.setPrefSize(500, 200);

        VBox budgetSection = new VBox(5);
        Label budgetTitle = new Label("Budget vs Actual");
        budgetTitle.setStyle("-fx-font-weight: bold;");
        budgetSection.getChildren().addAll(budgetTitle, budgetChart);

        // Summary statistics
        VBox statsSection = createStatisticsSection();

        mainLayout.getChildren().addAll(titleLabel, chartsLayout, budgetSection, statsSection);

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        Scene scene = new Scene(scrollPane, 900, 700);

        reportStage.setScene(scene);
        reportStage.show();
    }

    /**
     * Shows just the category breakdown pie chart
     */
    private void showCategoryReport() {
        if (expenses.isEmpty()) {
            showAlert("No Data", "No expenses recorded yet. Add some expenses first.");
            return;
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Category Breakdown");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        PieChart chart = createCategoryPieChart();
        chart.setPrefSize(400, 400);

        layout.getChildren().addAll(
                new Label("Expenses by Category"),
                chart,
                createCategoryStatsTable()
        );

        Scene scene = new Scene(new ScrollPane(layout), 500, 600);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Shows daily spending bar chart
     */
    private void showDailyReport() {
        if (expenses.isEmpty()) {
            showAlert("No Data", "No expenses recorded yet. Add some expenses first.");
            return;
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Daily Spending Report");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        BarChart<String, Number> chart = createDailyBarChart();
        chart.setPrefSize(600, 400);

        layout.getChildren().addAll(
                new Label("Daily Spending Pattern"),
                chart
        );

        Scene scene = new Scene(layout, 700, 500);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Shows budget vs actual comparison
     */
    private void showBudgetComparison() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Budget Comparison");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        BarChart<String, Number> chart = createBudgetComparisonChart();
        chart.setPrefSize(500, 300);

        layout.getChildren().addAll(
                new Label("Budget vs Actual Spending"),
                chart
        );

        Scene scene = new Scene(layout, 600, 400);
        stage.setScene(scene);
        stage.show();
    }

    // === CHART CREATION METHODS ===

    private PieChart createCategoryPieChart() {
        Map<String, Double> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        ExpenseRow::getCategory,
                        Collectors.summingDouble(ExpenseRow::getAmount)
                ));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        categoryTotals.forEach((category, total) ->
                pieData.add(new PieChart.Data(category + " ($" + String.format("%.2f", total) + ")", total))
        );

        PieChart chart = new PieChart(pieData);
        chart.setTitle("Expenses by Category");
        return chart;
    }

    private BarChart<String, Number> createDailyBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Amount ($)");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Daily Spending");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Daily Expenses");

        // Group expenses by date
        Map<String, Double> dailyTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        ExpenseRow::getDate,
                        Collectors.summingDouble(ExpenseRow::getAmount)
                ));

        // Sort by date and add to chart
        dailyTotals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> series.getData().add(
                        new XYChart.Data<>(entry.getKey(), entry.getValue())
                ));

        chart.getData().add(series);
        return chart;
    }

    private BarChart<String, Number> createBudgetComparisonChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Category");
        yAxis.setLabel("Amount ($)");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Budget vs Actual Spending");

        // Calculate category totals
        Map<String, Double> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        ExpenseRow::getCategory,
                        Collectors.summingDouble(ExpenseRow::getAmount)
                ));

        // Budget series (assuming equal distribution if no category budgets set)
        XYChart.Series<String, Number> budgetSeries = new XYChart.Series<>();
        budgetSeries.setName("Budget");

        // Actual expenses series
        XYChart.Series<String, Number> actualSeries = new XYChart.Series<>();
        actualSeries.setName("Actual");

        // Simple budget allocation (equal split among categories)
        double budgetPerCategory = currentTrip.getBudget() / 5; // 5 categories

        String[] categories = {"Transport", "Food", "Lodging", "Activities", "Other"};
        for (String category : categories) {
            budgetSeries.getData().add(new XYChart.Data<>(category, budgetPerCategory));
            actualSeries.getData().add(new XYChart.Data<>(category,
                    categoryTotals.getOrDefault(category, 0.0)));
        }

        chart.getData().addAll(budgetSeries, actualSeries);
        return chart;
    }

    private TableView<CategoryStat> createCategoryStatsTable() {
        TableView<CategoryStat> table = new TableView<>();

        TableColumn<CategoryStat, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> data.getValue().categoryProperty());

        TableColumn<CategoryStat, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(data -> data.getValue().amountProperty());

        TableColumn<CategoryStat, String> percentCol = new TableColumn<>("Percentage");
        percentCol.setCellValueFactory(data -> data.getValue().percentageProperty());

        table.getColumns().addAll(categoryCol, amountCol, percentCol);

        // Calculate totals
        double totalExpenses = expenses.stream().mapToDouble(ExpenseRow::getAmount).sum();
        Map<String, Double> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        ExpenseRow::getCategory,
                        Collectors.summingDouble(ExpenseRow::getAmount)
                ));

        ObservableList<CategoryStat> stats = FXCollections.observableArrayList();
        categoryTotals.forEach((category, amount) -> {
            double percentage = (amount / totalExpenses) * 100;
            stats.add(new CategoryStat(category,
                    String.format("$%.2f", amount),
                    String.format("%.1f%%", percentage)));
        });

        table.setItems(stats);
        return table;
    }

    private VBox createStatisticsSection() {
        VBox statsBox = new VBox(5);
        statsBox.setPadding(new Insets(10));
        statsBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1;");

        Label statsTitle = new Label("Trip Statistics");
        statsTitle.setStyle("-fx-font-weight: bold;");

        double totalExpenses = expenses.stream().mapToDouble(ExpenseRow::getAmount).sum();
        double avgDaily = calculateAverageDailySpending();
        String mostExpensiveCategory = findMostExpensiveCategory();

        Label totalLabel = new Label("Total Expenses: $" + String.format("%.2f", totalExpenses));
        Label avgLabel = new Label("Average Daily Spending: $" + String.format("%.2f", avgDaily));
        Label categoryLabel = new Label("Highest Spending Category: " + mostExpensiveCategory);
        Label budgetStatusLabel = new Label("Budget Status: " +
                (totalExpenses > currentTrip.getBudget() ? "Over Budget" : "Within Budget"));

        statsBox.getChildren().addAll(statsTitle, totalLabel, avgLabel, categoryLabel, budgetStatusLabel);
        return statsBox;
    }

    private double calculateAverageDailySpending() {
        if (expenses.isEmpty()) return 0;

        Set<String> uniqueDates = expenses.stream()
                .map(ExpenseRow::getDate)
                .collect(Collectors.toSet());

        if (uniqueDates.isEmpty()) return 0;

        double totalExpenses = expenses.stream().mapToDouble(ExpenseRow::getAmount).sum();
        return totalExpenses / uniqueDates.size();
    }

    private String findMostExpensiveCategory() {
        return expenses.stream()
                .collect(Collectors.groupingBy(
                        ExpenseRow::getCategory,
                        Collectors.summingDouble(ExpenseRow::getAmount)
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // === MODEL CLASSES (Keep your existing ExpenseRow and CategoryStat classes) ===

    public static class ExpenseRow {
        private final javafx.beans.property.SimpleStringProperty category =
                new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.SimpleDoubleProperty amount =
                new javafx.beans.property.SimpleDoubleProperty();
        private final javafx.beans.property.SimpleStringProperty date =
                new javafx.beans.property.SimpleStringProperty();

        public ExpenseRow(String category, double amount, String date) {
            this.category.set(category);
            this.amount.set(amount);
            this.date.set(date);
        }

        public String getCategory() { return category.get(); }
        public void setCategory(String v) { category.set(v); }
        public javafx.beans.property.StringProperty categoryProperty() { return category; }

        public double getAmount() { return amount.get(); }
        public void setAmount(double v) { amount.set(v); }
        public javafx.beans.property.DoubleProperty amountProperty() { return amount; }

        public String getDate() { return date.get(); }
        public void setDate(String v) { date.set(v); }
        public javafx.beans.property.StringProperty dateProperty() { return date; }
    }

    public static class CategoryStat {
        private final javafx.beans.property.SimpleStringProperty category =
                new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.SimpleStringProperty amount =
                new javafx.beans.property.SimpleStringProperty();
        private final javafx.beans.property.SimpleStringProperty percentage =
                new javafx.beans.property.SimpleStringProperty();

        public CategoryStat(String category, String amount, String percentage) {
            this.category.set(category);
            this.amount.set(amount);
            this.percentage.set(percentage);
        }

        public javafx.beans.property.StringProperty categoryProperty() { return category; }
        public javafx.beans.property.StringProperty amountProperty() { return amount; }
        public javafx.beans.property.StringProperty percentageProperty() { return percentage; }
    }
}