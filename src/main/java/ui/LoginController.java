package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.User;

public class LoginController {

    @FXML
    private TextField idField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private AppContext context;
    private JavaFxApp app;

    public void init(AppContext context, JavaFxApp app) {
        this.context = context;
        this.app = app;
    }

    @FXML
    private void onSignIn() {
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

    @FXML
    private void onBrowse() {
        app.showBrowse();
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
