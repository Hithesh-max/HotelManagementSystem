package com.example.hotelmanagement;

import javafx.animation.*; // Used for fade, pause, timeline, scale animations
import javafx.fxml.FXML; // Used to connect Java code with FXML components
import javafx.fxml.FXMLLoader; // Used to load another FXML file
import javafx.scene.Scene; // Represents the content shown in a stage
import javafx.scene.control.Label; // Label control
import javafx.scene.control.PasswordField; // Password input field
import javafx.scene.control.TextField; // Text input field
import javafx.scene.layout.StackPane; // Layout pane
import javafx.stage.Stage; // Main JavaFX window
import javafx.util.Duration; // Used to define animation time durations

public class LoginController {

    // This connects with the root StackPane in login-view.fxml
    @FXML private StackPane loginRoot;

    // Username input field from FXML
    @FXML private TextField usernameField;

    // Password input field from FXML
    @FXML private PasswordField passwordField;

    // Label used to show success/error messages
    @FXML private Label messageLabel;

    // Label used for animated typewriter text
    @FXML private Label typewriterLabel;

    // Array of slogans that will appear one by one in typewriter style
    private final String[] slogans = {
            "Excellence in Every Stay.",
            "Crafting Exceptional Guest Experiences.",
            "Luxury. Comfort. Precision.",
            "Redefining the Art of Hospitality.",
            "Experience Hospitality, Perfected.",
            "Elevating Hospitality Experiences."
    };

    // Keeps track of which slogan is currently being shown
    private int sloganIndex = 0;

    @FXML
    public void initialize() {
        // Fade animation for the whole login screen when it opens
        FadeTransition fade = new FadeTransition(Duration.millis(700), loginRoot);
        fade.setFromValue(0); // start invisible
        fade.setToValue(1);   // end fully visible

        // Slight zoom animation for a smoother opening effect
        ScaleTransition scale = new ScaleTransition(Duration.millis(550), loginRoot);
        scale.setFromX(0.98); // slightly smaller width at start
        scale.setFromY(0.98); // slightly smaller height at start
        scale.setToX(1.0);    // final normal width
        scale.setToY(1.0);    // final normal height

        // Start both animations
        fade.play();
        scale.play();

        // Start looping typewriter effect for slogans
        startTypewriterLoop();
    }

    private void startTypewriterLoop() {
        // Starts showing slogans one after another
        playNextSlogan();
    }

    private void playNextSlogan() {
        // Get current slogan text
        String text = slogans[sloganIndex];

        // Make label visible again
        typewriterLabel.setOpacity(1);

        // Clear old text before typing new one
        typewriterLabel.setText("");

        // Timeline is used to show letters one by one
        Timeline typing = new Timeline();

        // Add one keyframe for each character
        for (int i = 0; i < text.length(); i++) {
            final int index = i + 1; // substring ending position
            typing.getKeyFrames().add(
                    new KeyFrame(Duration.millis(55 * index),
                            e -> typewriterLabel.setText(text.substring(0, index)))
            );
        }

        // Pause after full slogan is typed
        PauseTransition hold = new PauseTransition(Duration.seconds(1.2));

        // Fade out effect after holding the slogan
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), typewriterLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        // After fade out finishes, move to next slogan
        fadeOut.setOnFinished(e -> {
            sloganIndex = (sloganIndex + 1) % slogans.length; // go to next slogan, loop back after last one
            PauseTransition gap = new PauseTransition(Duration.millis(250)); // small gap before next starts
            gap.setOnFinished(ev -> playNextSlogan());
            gap.play();
        });

        // After typing is done, start holding
        typing.setOnFinished(e -> hold.play());

        // After hold finishes, start fade out
        hold.setOnFinished(e -> fadeOut.play());

        // Start typing animation
        typing.play();
    }

    @FXML
    public void handleStaffLogin() {
        // Login button for staff calls loginWithRole with STAFF
        loginWithRole("STAFF");
    }

    @FXML
    public void handleCustomerLogin() {
        // Login button for customer calls loginWithRole with CUSTOMER
        loginWithRole("CUSTOMER");
    }

    @FXML
    public void handleCustomerSignup() {
        // Read username and password from text fields
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Check if fields are empty
        if (username.isEmpty() || password.isEmpty()) {
            setMessage("Enter username and password.", false);
            return;
        }

        // Password should be at least 4 characters
        if (password.length() < 4) {
            setMessage("Password should be at least 4 characters.", false);
            return;
        }

        // Try to add customer to users file
        if (UserManager.addCustomer(username, password)) {
            setMessage("Customer signup successful. Please login.", true);

            // Clear fields after successful signup
            usernameField.clear();
            passwordField.clear();
        } else {
            // Username already exists
            setMessage("Username already exists.", false);
        }
    }

    private void loginWithRole(String role) {
        // Read username and password
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Validate empty fields
        if (username.isEmpty() || password.isEmpty()) {
            setMessage("Enter username and password.", false);
            return;
        }

        // Check if username/password/role match stored data
        if (!UserManager.validateUser(username, password, role)) {
            setMessage("Invalid " + role.toLowerCase() + " username or password.", false);
            passwordField.clear(); // clear only password for security
            return;
        }

        // Save current session details
        SessionManager.setSession(username, role);

        try {
            // Get current stage (window)
            Stage stage = (Stage) usernameField.getScene().getWindow();

            // Load main application view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 800);

            // Add CSS files
            scene.getStylesheets().add(getClass().getResource("common.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("customer-theme.css").toExternalForm());

            // Set new scene
            stage.setScene(scene);

            // Update title according to role
            stage.setTitle("Hotel Management System - " + role);

            // Put window at center of screen
            stage.centerOnScreen();
        } catch (Exception e) {
            // If loading main screen fails
            e.printStackTrace();
            setMessage("Failed to open main application.", false);
        }

        // Clear login fields after successful login attempt
        usernameField.clear();
        passwordField.clear();
    }

    private void setMessage(String text, boolean success) {
        // Set text in message label
        messageLabel.setText(text);

        // Remove old style classes so message style doesn't conflict
        messageLabel.getStyleClass().removeAll("message-error", "message-success");

        // Add style depending on success or error
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