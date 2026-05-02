package com.hr;

import com.hr.ui.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        showLogin(stage);
    }

    public static void showLogin(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/hr/login.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(MainApp.class.getResource("/styles.css").toExternalForm());
        LoginController ctrl = loader.getController();
        ctrl.setStage(stage);
        stage.setTitle("HR Management System — Sign In");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        if (!stage.isShowing()) {
            stage.setWidth(1100);
            stage.setHeight(700);
            stage.centerOnScreen();
            stage.show();
        }
    }

    public static void showMain(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/hr/main.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(MainApp.class.getResource("/styles.css").toExternalForm());
        stage.setTitle("HR Management System");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setScene(scene);
        if (!stage.isShowing()) {
            stage.setWidth(1200);
            stage.setHeight(750);
            stage.centerOnScreen();
            stage.show();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
