package com.hr.ui;

import com.hr.MainApp;
import com.hr.model.UserAccount;
import com.hr.service.AuthService;
import com.hr.service.SessionManager;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;

public class LoginController {

    @FXML private TextField     tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private Label         lblError;
    @FXML private Button        btnSignIn;
    @FXML private VBox          loginCard;

    private Stage       stage;
    private AuthService authService;

    @FXML
    public void initialize() {
        try {
            authService = new AuthService();
        } catch (SQLException e) {
            showError("Cannot connect to database: " + e.getMessage());
        }

        tfUsername.setOnAction(e -> pfPassword.requestFocus());
        pfPassword.setOnAction(e -> handleLogin());

        // Slide-in entrance for the card
        loginCard.setOpacity(0);
        loginCard.setTranslateY(24);
        Timeline entrance = new Timeline(
            new KeyFrame(Duration.millis(500),
                new KeyValue(loginCard.opacityProperty(),    1,  Interpolator.EASE_OUT),
                new KeyValue(loginCard.translateYProperty(), 0,  Interpolator.EASE_OUT)
            )
        );
        entrance.play();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleLogin() {
        hideError();
        String username = tfUsername.getText().trim();
        String password = pfPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        btnSignIn.setDisable(true);
        btnSignIn.setText("Signing in...");

        try {
            UserAccount user = authService.login(username, password);
            SessionManager.getInstance().setCurrentUser(user);

            // Fade out and switch to main
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), loginCard);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                try {
                    MainApp.showMain(stage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            fadeOut.play();

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            shakeCard();
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        } finally {
            btnSignIn.setDisable(false);
            btnSignIn.setText("Sign In");
            pfPassword.clear();
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void hideError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    private void shakeCard() {
        Timeline shake = new Timeline(
            new KeyFrame(Duration.millis(0),   new KeyValue(loginCard.translateXProperty(), 0)),
            new KeyFrame(Duration.millis(60),  new KeyValue(loginCard.translateXProperty(), -12)),
            new KeyFrame(Duration.millis(120), new KeyValue(loginCard.translateXProperty(),  12)),
            new KeyFrame(Duration.millis(180), new KeyValue(loginCard.translateXProperty(), -8)),
            new KeyFrame(Duration.millis(240), new KeyValue(loginCard.translateXProperty(),  8)),
            new KeyFrame(Duration.millis(300), new KeyValue(loginCard.translateXProperty(), 0))
        );
        shake.play();
    }
}
