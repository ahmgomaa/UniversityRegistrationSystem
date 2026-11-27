package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import model.Student;

public class StudentSettingsController {

    @FXML
    private PasswordField oldField;

    @FXML
    private PasswordField newField;

    @FXML
    private Label statusLabel;

    private AppContext context;
    private JavaFxApp app;
    private Student student;

    public void init(AppContext context, JavaFxApp app, Student student) {
        this.context = context;
        this.app = app;
        this.student = student;
    }

    @FXML
    private void onChange() {
        try {
            student.changePassword(oldField.getText(), newField.getText());
            context.getStore().save();
            if (SaveCheck.passwordSaved(student.getUserId(),
                    student.getPasswordHash())) {
                UiKit.showStatus(statusLabel, true, "Password updated.");
            } else {
                UiKit.showStatus(statusLabel, false,
                        "The change was not saved. Try again — if it keeps "
                                + "failing, restart the app or contact the registrar.");
            }
        } catch (IllegalArgumentException ex) {
            UiKit.showStatus(statusLabel, false, ex.getMessage());
        }
    }

    @FXML
    private void onSignOut() {
        app.signOut();
    }
}
