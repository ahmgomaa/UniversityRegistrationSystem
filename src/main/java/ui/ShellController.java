package ui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class ShellController {

    @FXML
    private BorderPane shellRoot;

    @FXML
    private Label titleLabel;

    @FXML
    private Button appearanceButton;

    private JavaFxApp app;

    public void init(JavaFxApp app) {
        this.app = app;
    }

    public void show(String title, Node content, boolean dark) {
        titleLabel.setText(title);
        appearanceButton.setText(dark ? "Light" : "Dark");
        shellRoot.setCenter(content);
    }

    @FXML
    private void onToggleAppearance() {
        app.toggleAppearance();
        appearanceButton.setText(app.isDarkAppearance() ? "Light" : "Dark");
    }
}
