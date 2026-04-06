package com.example.hotelmanagement;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    @FXML private StackPane loginRoot;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Label typewriterLabel;

    private final String[] slogans = {
            "Excellence in Every Stay.",
            "Crafting Exceptional Guest Experiences.",
            "Luxury. Comfort. Precision.",
            "Redefining the Art of Hospitality.",
            "Experience Hospitality, Perfected.",
            "Elevating Hospitality Experiences."
    };

    private int sloganIndex = 0;

    @FXML
    public void initialize() {
        FadeTransition fade = new FadeTransition(Duration.millis(700), loginRoot);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(550), loginRoot);
        scale.setFromX(0.98);
        scale.setFromY(0.98);
        scale.setToX(1.0);
        scale.setToY(1.0);

        fade.play();
        scale.play();

        startTypewriterLoop();
    }

    private void startTypewriterLoop() {
        playNextSlogan();
    }

    private void playNextSlogan() {
        String text = slogans[sloganIndex];
        typewriterLabel.setOpacity(1);
        typewriterLabel.setText("");

        Timeline typing = new Timeline();
        for (int i = 0; i < text.length(); i++) {
            final int index = i + 1;
            typing.getKeyFrames().add(
                    new KeyFrame(Duration.millis(55 * index),
                            e -> typewriterLabel.setText(text.substring(0, index)))
            );
        }

        PauseTransition hold = new PauseTransition(Duration.seconds(1.2));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), typewriterLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeOut.setOnFinished(e -> {
            sloganIndex = (sloganIndex + 1) % slogans.length;
            PauseTransition gap = new PauseTransition(Duration.millis(250));
            gap.setOnFinished(ev -> playNextSlogan());
            gap.play();
        });

        typing.setOnFinished(e -> hold.play());
        hold.setOnFinished(e -> fadeOut.play());

        typing.play();
    }

    @FXML
    public void handleStaffLogin() {
        loginWithRole("STAFF");
    }

    @FXML
    public void handleCustomerLogin() {
        loginWithRole("CUSTOMER");
    }

    @FXML
    public void handleCustomerSignup() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            setMessage("Enter username and password.", false);
            return;
        }

        if (password.length() < 4) {
            setMessage("Password should be at least 4 characters.", false);
            return;
        }

        if (UserManager.addCustomer(username, password)) {
            setMessage("Customer signup successful. Please login.", true);
            usernameField.clear();
            passwordField.clear();
        } else {
            setMessage("Username already exists.", false);
        }
    }

    private void loginWithRole(String role) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            setMessage("Enter username and password.", false);
            return;
        }

        if (!UserManager.validateUser(username, password, role)) {
            setMessage("Invalid " + role.toLowerCase() + " username or password.", false);
            passwordField.clear();
            return;
        }

        SessionManager.setSession(username, role);

        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 800);
            scene.getStylesheets().add(getClass().getResource("common.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("customer-theme.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Hotel Management System - " + role);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            setMessage("Failed to open main application.", false);
        }

        usernameField.clear();
        passwordField.clear();
    }

    private void setMessage(String text, boolean success) {
        messageLabel.setText(text);
        messageLabel.getStyleClass().removeAll("message-error", "message-success");
        messageLabel.getStyleClass().add(success ? "message-success" : "message-error");
    }
}
/*
package com.example.hotelmanagement;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    @FXML
    private StackPane loginRoot;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        FadeTransition fade = new FadeTransition(Duration.seconds(1.2), loginRoot);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(1.0), loginRoot);
        scale.setFromX(0.96);
        scale.setFromY(0.96);
        scale.setToX(1.0);
        scale.setToY(1.0);

        fade.play();
        scale.play();
    }

    @FXML
    public void handleStaffLogin() {
        loginWithRole("STAFF");
    }

    @FXML
    public void handleCustomerLogin() {
        loginWithRole("CUSTOMER");
    }

    @FXML
    public void handleCustomerSignup() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Enter username and password.");
            return;
        }

        if (UserManager.addCustomer(username, password)) {
            messageLabel.setText("Customer signup successful. Please login.");
            usernameField.clear();
            passwordField.clear();
        } else {
            messageLabel.setText("Username already exists.");
        }
    }

    private void loginWithRole(String role) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Enter username and password.");
            return;
        }

        if (UserManager.validateUser(username, password, role)) {
            SessionManager.setSession(username, role);

            try {
                Stage stage = (Stage) usernameField.getScene().getWindow();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
                Scene scene = new Scene(loader.load(), 1200, 760);

                scene.getStylesheets().add(getClass().getResource("common.css").toExternalForm());

                if ("STAFF".equals(role)) {
                    scene.getStylesheets().add(getClass().getResource("staff-theme.css").toExternalForm());
                } else {
                    scene.getStylesheets().add(getClass().getResource("customer-theme.css").toExternalForm());
                }

                stage.setScene(scene);
                stage.setTitle("Hotel Management System - " + role);
                stage.centerOnScreen();

            } catch (Exception e) {
                e.printStackTrace();
                messageLabel.setText("Failed to open main application.");
            }
        } else {
            messageLabel.setText("Invalid " + role.toLowerCase() + " username or password.");
        }

        usernameField.clear();
        passwordField.clear();
    }
}


/*
package com.example.hotelmanagement;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    @FXML
    private StackPane loginRoot;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        FadeTransition fade = new FadeTransition(Duration.seconds(1.2), loginRoot);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(1.0), loginRoot);
        scale.setFromX(0.96);
        scale.setFromY(0.96);
        scale.setToX(1.0);
        scale.setToY(1.0);

        fade.play();
        scale.play();
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (UserManager.validateUser(username, password)) {
            try {
                Stage stage = (Stage) usernameField.getScene().getWindow();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
                Scene scene = new Scene(loader.load(), 1200, 760);
                scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

                stage.setScene(scene);
                stage.setTitle("Hotel Management System");
                stage.centerOnScreen();
            } catch (Exception e) {
                e.printStackTrace();
                messageLabel.setText("Failed to open main application.");
            }
        } else {
            messageLabel.setText("Invalid username or password.");
        }

        usernameField.clear();
        passwordField.clear();
    }

    @FXML
    public void handleSignup() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Enter username and password.");
            return;
        }

        if (UserManager.addUser(username, password)) {
            messageLabel.setText("Signup successful. Please login.");
        } else {
            messageLabel.setText("User already exists.");
        }

        usernameField.clear();
        passwordField.clear();
    }
}

 */