package ui;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Small iOS-style building blocks shared by every screen: large titles,
 * grouped cards, system-blue buttons and error / success banners.
 * Colours follow the draft: system blue #007AFF, grouped backgrounds and
 * 12px rounded cards, on top of the AtlantaFX Cupertino theme.
 */
public final class UiKit {

    /** iOS system blue. */
    public static final String ACCENT = "#007AFF";

    private UiKit() {
    }

    /** Large title, like an iOS navigation bar title. */
    public static Label largeTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 28));
        return label;
    }

    /** Small grey section heading above a grouped card. */
    public static Label sectionLabel(String text) {
        Label label = new Label(text.toUpperCase());
        label.setFont(Font.font("System", FontWeight.BOLD, 12));
        label.setStyle("-fx-text-fill: #8E8E93;");
        return label;
    }

    /** A grouped iOS-style card: rounded, with padding. */
    public static VBox card(javafx.scene.Node... children) {
        VBox box = new VBox(8);
        box.getChildren().addAll(children);
        box.setPadding(new Insets(14));
        box.setStyle("-fx-background-radius: 12;"
                + "-fx-border-radius: 12;"
                + "-fx-border-color: #D1D1D6;"
                + "-fx-border-width: 0.5;");
        return box;
    }

    /** A filled system-blue button. */
    public static Button primaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + ACCENT + ";"
                + "-fx-text-fill: white;"
                + "-fx-background-radius: 10;"
                + "-fx-font-weight: bold;"
                + "-fx-padding: 10 18;");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    /** A plain button (secondary action). */
    public static Button plainButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-radius: 10; -fx-padding: 10 18;");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    /** A red banner for errors (for example: failed registration rules). */
    public static Label errorBanner(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-background-color: #FFECEC;"
                + "-fx-text-fill: #C00000;"
                + "-fx-background-radius: 10;"
                + "-fx-padding: 10 12;");
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    /** A green banner for success messages. */
    public static Label successBanner(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-background-color: #E6F7E6;"
                + "-fx-text-fill: #1E7A1E;"
                + "-fx-background-radius: 10;"
                + "-fx-padding: 10 12;");
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    /** Format a CGPA / GPA with two decimals. */
    public static String formatGpa(double value) {
        return String.format("%.2f", value);
    }

    /** A simple information dialog. */
    public static void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** A simple error dialog. */
    public static void error(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
