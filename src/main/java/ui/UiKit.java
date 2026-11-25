package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public final class UiKit {

    public static final String ACCENT = "#007AFF";

    public static final String GREEN = "#34C759";

    public static final String ORANGE = "#FF9500";

    public static final String RED = "#FF3B30";

    public static final String PURPLE = "#AF52DE";

    public static final String GRAY = "#8E8E93";

    public static final String TEXT = "#1C1C1E";

    public static final String GROUPED_BG = "#F2F2F7";

    public static final String SEPARATOR = "#E5E5EA";

    private static final String GREY_FILL = "rgba(120,120,128,0.12)";

    private UiKit() {
    }

    public static Font textFont(double size) {
        return Font.font("SF Pro Text", FontWeight.NORMAL, size);
    }

    public static Font textFont(FontWeight weight, double size) {
        return Font.font("SF Pro Text", weight, size);
    }

    public static Font displayFont(double size) {
        return Font.font("SF Pro Display", FontWeight.BOLD, size);
    }

    public static void applyGroupedBackground(Region root) {
        root.setStyle("-fx-background-color: " + GROUPED_BG + ";");
    }

    public static Label largeTitle(String text) {
        Label label = new Label(text);
        label.setFont(displayFont(28));
        label.setStyle("-fx-text-fill: " + TEXT + ";");
        return label;
    }

    public static Label title(String text) {
        Label label = new Label(text);
        label.setFont(displayFont(22));
        label.setStyle("-fx-text-fill: " + TEXT + ";");
        return label;
    }

    public static Label caption(String text) {
        Label label = new Label(text);
        label.setFont(textFont(13));
        label.setStyle("-fx-text-fill: " + GRAY + ";");
        label.setWrapText(true);
        return label;
    }

    public static Label sectionLabel(String text) {
        Label label = new Label(text.toUpperCase());
        label.setFont(textFont(FontWeight.SEMI_BOLD, 12));
        label.setStyle("-fx-text-fill: " + GRAY + ";");
        label.setPadding(new Insets(0, 0, 0, 4));
        return label;
    }

    public static VBox card(javafx.scene.Node... children) {
        VBox box = new VBox(10);
        box.getChildren().addAll(children);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color: white;"
                + "-fx-background-radius: 14;");
        return box;
    }

    public static VBox statCard(String value, String captionText, String color) {
        Label valueLabel = new Label(value);
        valueLabel.setFont(displayFont(24));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        Label captionLabel = new Label(captionText.toUpperCase());
        captionLabel.setFont(textFont(FontWeight.SEMI_BOLD, 11));
        captionLabel.setStyle("-fx-text-fill: " + GRAY + ";");
        VBox box = new VBox(4, valueLabel, captionLabel);
        box.setPadding(new Insets(16));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: white;"
                + "-fx-background-radius: 14;");
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    public static HBox statRow(VBox... cards) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        for (VBox card : cards) {
            HBox cell = new HBox(card);
            HBox.setHgrow(cell, Priority.ALWAYS);
            HBox.setHgrow(card, Priority.ALWAYS);
            cell.setMaxWidth(Double.MAX_VALUE);
            cell.setFillHeight(true);
            row.getChildren().add(cell);
        }
        return row;
    }

    public static VBox insetCard(javafx.scene.Node... children) {
        VBox box = new VBox(0);
        box.getChildren().addAll(children);
        box.setStyle("-fx-background-color: white;"
                + "-fx-background-radius: 14;");
        return box;
    }

    public static Region separator() {
        Region line = new Region();
        line.setPrefHeight(1);
        line.setMinHeight(1);
        line.setMaxWidth(Double.MAX_VALUE);
        line.setStyle("-fx-background-color: " + SEPARATOR + ";");
        VBox.setMargin(line, new Insets(0, 0, 0, 16));
        return line;
    }

    public static Label columnLabel(String text, double width) {
        Label label = new Label(text);
        label.setFont(textFont(14));
        label.setStyle("-fx-text-fill: " + TEXT + ";");
        label.setPrefWidth(width);
        label.setMinWidth(width);
        label.setMaxWidth(width);
        return label;
    }

    public static Label columnHeader(String text, double width) {
        Label label = columnLabel(text.toUpperCase(), width);
        label.setFont(textFont(FontWeight.SEMI_BOLD, 11));
        label.setStyle("-fx-text-fill: " + GRAY + ";");
        return label;
    }

    public static void styleInsetList(ListView<String> list) {
        list.setStyle("-fx-background-color: white;"
                + "-fx-background-radius: 14;"
                + "-fx-border-color: " + SEPARATOR + ";"
                + "-fx-border-radius: 14;"
                + "-fx-border-width: 1;");
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

    public static Label pill(String text, String color) {
        Label label = new Label(text);
        label.setFont(textFont(FontWeight.SEMI_BOLD, 11));
        label.setStyle("-fx-background-color: " + color + ";"
                + "-fx-text-fill: white;"
                + "-fx-background-radius: 999;"
                + "-fx-padding: 3 10;");
        return label;
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

    public static String statusColor(String status) {
        switch (status) {
            case "ACTIVE":
            case "ENROLLED":
                return GREEN;
            case "PROBATION":
                return ORANGE;
            case "GRADUATED":
            case "COMPLETED":
                return ACCENT;
            case "SUSPENDED":
            case "WITHDRAWN":
                return RED;
            default:
                return GRAY;
        }
    }

    public static Label statusPill(String status) {
        return softPill(status, statusColor(status));
    }

    private static String tint(String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        return "rgba(" + r + "," + g + "," + b + ",0.15)";
    }

    public static HBox progressBar(double fraction, String color) {
        double clamped = Math.max(0, Math.min(1, fraction));
        Region filled = new Region();
        filled.setStyle("-fx-background-color: " + color + ";"
                + "-fx-background-radius: 999;");
        filled.setPrefSize(320 * clamped, 6);
        filled.setMinHeight(6);
        Region rest = new Region();
        rest.setStyle("-fx-background-color: " + GREY_FILL + ";"
                + "-fx-background-radius: 999;");
        rest.setPrefSize(320 * (1 - clamped), 6);
        rest.setMinHeight(6);
        HBox bar = new HBox(filled, rest);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(4, 0, 0, 0));
        return bar;
    }

    public static Button primaryButton(String text) {
        Button button = new Button(text);
        button.setFont(textFont(FontWeight.SEMI_BOLD, 15));
        button.setStyle("-fx-background-color: " + ACCENT + ";"
                + "-fx-text-fill: white;"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 12 18;");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    public static Button plainButton(String text) {
        Button button = new Button(text);
        button.setFont(textFont(FontWeight.SEMI_BOLD, 15));
        button.setStyle("-fx-background-color: " + GREY_FILL + ";"
                + "-fx-text-fill: " + ACCENT + ";"
                + "-fx-background-radius: 12;"
                + "-fx-padding: 12 18;");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    public static Button navButton(String text) {
        Button button = new Button(text);
        button.setFont(textFont(15));
        button.setStyle(navStyle(false));
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    public static void setNavSelected(Button button, boolean selected) {
        button.setStyle(navStyle(selected));
    }

    private static String navStyle(boolean selected) {
        if (selected) {
            return "-fx-background-color: " + tint(ACCENT) + ";"
                    + "-fx-text-fill: " + ACCENT + ";"
                    + "-fx-font-weight: bold;"
                    + "-fx-background-radius: 10; -fx-padding: 9 12;"
                    + "-fx-alignment: CENTER_LEFT;";
        }
        return "-fx-background-color: transparent;"
                + "-fx-text-fill: " + TEXT + ";"
                + "-fx-background-radius: 10; -fx-padding: 9 12;"
                + "-fx-alignment: CENTER_LEFT;";
    }

    public static Label statusLabel() {
        Label label = new Label();
        label.setFont(textFont(14));
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    public static void showStatus(Label label, boolean ok, String text) {
        label.setText((ok ? "\u2713 " : "\u2717 ") + text);
        label.setStyle(ok
                ? "-fx-background-color: " + tint(GREEN) + ";"
                        + "-fx-text-fill: #1E7A1E;"
                        + "-fx-background-radius: 10; -fx-padding: 10 12;"
                : "-fx-background-color: " + tint(RED) + ";"
                        + "-fx-text-fill: #C00000;"
                        + "-fx-background-radius: 10; -fx-padding: 10 12;");
    }

    public static Label errorBanner(String text) {
        Label label = new Label(text);
        label.setFont(textFont(14));
        label.setWrapText(true);
        label.setStyle("-fx-background-color: " + tint(RED) + ";"
                + "-fx-text-fill: #C00000;"
                + "-fx-background-radius: 10;"
                + "-fx-padding: 10 12;");
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    public static Label successBanner(String text) {
        Label label = new Label(text);
        label.setFont(textFont(14));
        label.setWrapText(true);
        label.setStyle("-fx-background-color: " + tint(GREEN) + ";"
                + "-fx-text-fill: #1E7A1E;"
                + "-fx-background-radius: 10;"
                + "-fx-padding: 10 12;");
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    public static Label emptyState(String text) {
        Label label = new Label(text);
        label.setFont(textFont(15));
        label.setStyle("-fx-text-fill: " + GRAY + ";");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setPadding(new Insets(24, 0, 24, 0));
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

    public static void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void error(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

