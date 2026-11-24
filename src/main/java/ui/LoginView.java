package ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import model.User;

/**
 * Sign-in screen: user ID + 4-digit password.
 * The lookup and the password check are the same as in ConsoleUI:
 * try the students, then the instructors, then the academic staff.
 */
public class LoginView {

    private final AppContext context;
    private final JavaFxApp app;

    public LoginView(AppContext context, JavaFxApp app) {
        this.context = context;
        this.app = app;
    }

    public VBox build() {
        Label hint = new Label("Sign in with your user ID and 4-digit password.\n"
                + "Students look like CS-1001, instructors IN-101, staff AS-11.");
        hint.setWrapText(true);

        TextField idField = new TextField();
        idField.setPromptText("User ID (e.g. CS-1001)");
        idField.setId("login-id-field");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (4 digits)");
        passwordField.setId("login-password-field");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #C00000;");
        errorLabel.setWrapText(true);

        Button signInButton = UiKit.primaryButton("Sign In");
        signInButton.setId("login-signin-button");
        signInButton.setOnAction(e -> {
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
        });

        Button browseButton = UiKit.plainButton("Browse Sections as Visitor");
        browseButton.setOnAction(e -> app.showBrowse());

        VBox form = UiKit.card(hint, idField, passwordField, errorLabel, signInButton, browseButton);
        form.setMaxWidth(420);

        VBox root = new VBox(16, form);
        root.setPadding(new Insets(20));
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
