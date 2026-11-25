package ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.User;

public class LoginView {

    private final AppContext context;
    private final JavaFxApp app;

    public LoginView(AppContext context, JavaFxApp app) {
        this.context = context;
        this.app = app;
    }

    public VBox build() {
        Label wordmark = UiKit.largeTitle("University Registration");
        Label tagline = UiKit.caption(
                "Register for courses, track your transcript and stay on top of "
                        + "graduation — all in one place.");
        tagline.setMaxWidth(420);
        tagline.setAlignment(Pos.CENTER);

        TextField idField = new TextField();
        idField.setPromptText("User ID (e.g. CS-1001)");
        idField.setId("login-id-field");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (4 digits)");
        passwordField.setId("login-password-field");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #C00000;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(Double.MAX_VALUE);

        Button signInButton = UiKit.primaryButton("Sign In");
        signInButton.setId("login-signin-button");
        signInButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String id = idField.getText() == null ? "" : idField.getText().trim();
                String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
                if (id.isEmpty() || password.isEmpty()) {
                    errorLabel.setText("Enter your user ID and password.");
                    return;
                }
                User user = findUserById(id);
                if (user == null || !user.verifyPassword(password)) {
                    errorLabel.setText("Invalid user ID or password.");
                    return;
                }
                app.signedIn(user);
            }
        });

        idField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                signInButton.fire();
            }
        });
        passwordField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                signInButton.fire();
            }
        });

        Label hint = UiKit.caption(
                "Students look like CS-1001, instructors IN-101, staff AS-11. "
                        + "The password is 4 digits.");

        VBox form = UiKit.card(idField, passwordField, errorLabel, signInButton, hint);
        form.setMaxWidth(420);

        Button browseButton = UiKit.plainButton("Browse Sections as Visitor");
        browseButton.setMaxWidth(420);
        browseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                app.showBrowse();
            }
        });

        VBox center = new VBox(14, wordmark, tagline, form, browseButton);
        center.setAlignment(Pos.TOP_CENTER);
        center.setMaxWidth(460);

        HBox wrapper = new HBox(center);
        wrapper.setAlignment(Pos.CENTER);
        HBox.setHgrow(center, Priority.NEVER);

        VBox root = new VBox(wrapper);
        root.setPadding(new Insets(32, 20, 20, 20));
        UiKit.applyGroupedBackground(root);
        VBox.setVgrow(wrapper, Priority.ALWAYS);
        Region topSpacer = new Region();
        root.getChildren().add(0, topSpacer);
        return root;
    }

    private User findUserById(String id) {
        User user = context.getStore().findById(id);
        if (user != null) {
            return user;
        }
        user = context.getStore().findInstructorById(id);
        if (user != null) {
            return user;
        }
        return context.getStore().findStaffById(id);
    }
}

