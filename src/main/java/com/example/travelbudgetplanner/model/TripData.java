package com.example.travelbudgetplanner.model;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Main trip data model that contains all trip information
 */
public class TripData {
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private double budget;
    private List<Expense> expenses;

    // Default constructor
    public TripData() {
        this.expenses = new ArrayList<>();
    }

    // Constructor with parameters
    public TripData(String destination, LocalDate startDate, LocalDate endDate, double budget) {
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.expenses = new ArrayList<>();
    }

    // Getters and Setters
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public List<Expense> getExpenses() { return expenses; }
    public void setExpenses(List<Expense> expenses) { this.expenses = expenses; }

    public void addExpense(Expense expense) {
        this.expenses.add(expense);
    }

    public void removeExpense(Expense expense) {
        this.expenses.remove(expense);
    }
}