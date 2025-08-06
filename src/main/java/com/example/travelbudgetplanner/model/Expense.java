package com.example.travelbudgetplanner.model;


import java.time.LocalDate;

/**
 * Individual expense model
 */
public class Expense {
    private String category;
    private double amount;
    private LocalDate date;
    private String description; // Optional field for future use

    // Default constructor
    public Expense() {}

    // Constructor
    public Expense(String category, double amount, LocalDate date) {
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    // Getters and Setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return String.format("%s: $%.2f on %s", category, amount, date);
    }
}