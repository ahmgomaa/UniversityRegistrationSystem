package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public final class UiKit {

    public static final String ACCENT = "#007AFF";

    public static final String GREEN = "#34C759";

    public static final String ORANGE = "#FF9500";

    public static final String RED = "#FF3B30";

    public static final String GRAY = "#8E8E93";

    public static final String SEPARATOR = "#E5E5EA";

    private UiKit() {
    }

    public static Font textFont(FontWeight weight, double size) {
        return Font.font("SF Pro Text", weight, size);
    }

    public static Font textFont(double size) {
        return Font.font("SF Pro Text", FontWeight.NORMAL, size);
    }

    public static Region separator() {
        Region line = new Region();
        line.setPrefHeight(1);
        line.setMinHeight(1);
        line.setMaxWidth(Double.MAX_VALUE);
        line.getStyleClass().add("row-separator");
        VBox.setMargin(line, new Insets(0, 0, 0, 16));
        return line;
    }

    public static Label columnLabel(String text, double width) {
        Label label = new Label(text);
        label.setFont(textFont(14));
        label.setPrefWidth(width);
        label.setMinWidth(width);
        label.setMaxWidth(width);
        return label;
    }

    public static void prepareList(ListView<String> list) {
        list.setFixedCellSize(34);
        sizeListToContent(list);
        list.itemsProperty().addListener(
                new javafx.beans.value.ChangeListener<javafx.collections.ObservableList<String>>() {
                    @Override
                    public void changed(
                            javafx.beans.value.ObservableValue<? extends javafx.collections.ObservableList<String>> obs,
                            javafx.collections.ObservableList<String> oldItems,
                            javafx.collections.ObservableList<String> newItems) {
                        sizeListToContent(list);
                    }
                });
    }

    public static void sizeListToContent(ListView<?> list) {
        int rows = list.getItems() == null ? 0 : list.getItems().size();
        double wanted = Math.max(rows, 1) * list.getFixedCellSize() + 4;
        list.setPrefHeight(Math.min(wanted, 460));
    }

    public static Label softPill(String text, String color) {
        Label label = new Label(text);
        label.setFont(textFont(FontWeight.SEMI_BOLD, 11));
        label.setStyle("-fx-background-color: " + tint(color) + ";"
                + "-fx-text-fill: " + color + ";"
                + "-fx-background-radius: 999;"
                + "-fx-padding: 3 10;");
        return label;
    }

    private static String tint(String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        return "rgba(" + r + "," + g + "," + b + ",0.15)";
    }

    public static void showStatus(Label label, boolean ok, String text) {
        label.setText((ok ? "\u2713 " : "\u2717 ") + text);
        label.getStyleClass().removeAll("status-ok", "status-bad");
        label.getStyleClass().add(ok ? "status-ok" : "status-bad");
    }

    public static void clearStatus(Label label) {
        label.setText("");
        label.getStyleClass().removeAll("status-ok", "status-bad");
    }

    public static Label emptyState(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("empty-state");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    public static String formatGpa(double value) {
        return String.format("%.2f", value);
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        return result.isPresent()
                && result.get() == javafx.scene.control.ButtonType.OK;
    }

    public static void error(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
