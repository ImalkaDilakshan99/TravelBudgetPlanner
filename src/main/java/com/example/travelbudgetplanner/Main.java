package com.example.travelbudgetplanner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML file
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/travelbudgetplanner/trip_view.fxml"));
            Parent tripViewRoot = fxmlLoader.load();

            // Create the scene
            Scene tripScene = new Scene(tripViewRoot);

            // Add CSS file
            tripScene.getStylesheets().add(getClass().getResource("/com/example/travelbudgetplanner/travel-budget-planner.css").toExternalForm());

            // Configure the primary stage
            primaryStage.setScene(tripScene);
            primaryStage.setTitle("Travel Budget Planner");
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(700);
            primaryStage.setResizable(true);
            primaryStage.show();

        } catch (IOException ex) {
            System.err.println("Error loading FXML file: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            System.err.println("Error loading CSS file: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}