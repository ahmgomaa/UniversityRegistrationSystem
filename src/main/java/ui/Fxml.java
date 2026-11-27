package ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public final class Fxml<C> {

    private final Parent root;
    private final C controller;

    private Fxml(Parent root, C controller) {
        this.root = root;
        this.controller = controller;
    }

    public static <C> Fxml<C> load(String name) {
        FXMLLoader loader = new FXMLLoader(Fxml.class.getResource("/ui/" + name + ".fxml"));
        try {
            Parent root = loader.load();
            C controller = loader.getController();
            return new Fxml<C>(root, controller);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot load " + name + ".fxml: " + e.getMessage(), e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public C getController() {
        return controller;
    }
}
