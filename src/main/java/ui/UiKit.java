package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Small iOS-style building blocks shared by every screen: titles,
 * grouped cards, stat cards, pills, system-blue buttons, progress bars
 * and error / success banners. Colours follow the draft: system blue
 * #007AFF, grouped backgrounds and 12px rounded cards, on top of the
 * AtlantaFX Cupertino theme.
 *
 * Only Lecture 7/8 building blocks are used (Label, Button, VBox/HBox,
 * Region, inline CSS) — no custom cells, no extra controls.
 */
public final class UiKit {

    /** iOS system blue. */
    public static final String ACCENT = "#007AFF";
    /** iOS system green (success, good standing). */
    public static final String GREEN = "#34C759";
    /** iOS system orange (warning, attention). */
    public static final String ORANGE = "#FF9500";
    /** iOS system red (error, failure). */
    public static final String RED = "#FF3B30";
    /** iOS system purple (accent for graduation / completed). */
    public static final String PURPLE = "#AF52DE";
    /** iOS secondary label grey. */
    public static final String GRAY = "#8E8E93";

    private static final String CARD_BORDER = "#D1D1D6";

    private UiKit() {
    }

    // --------------------------------------------------------------- Titles

    /** Large title, like an iOS navigation bar title. */
    public static Label largeTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 28));
        return label;
    }

    /** Screen title inside a portal (smaller than the bar title). */
    public static Label title(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 22));
        return label;
    }

    /** Supporting line under a title. */
    public static Label caption(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + GRAY + ";");
        label.setWrapText(true);
        return label;
    }

    /** Small grey section heading above a grouped card. */
    public static Label sectionLabel(String text) {
        Label label = new Label(text.toUpperCase());
        label.setFont(Font.font("System", FontWeight.BOLD, 12));
        label.setStyle("-fx-text-fill: " + GRAY + ";");
        return label;
    }

    // ---------------------------------------------------------------- Cards

    /** A grouped iOS-style card: rounded, with padding. The faint fill
     *  reads as a card on the light theme and stays subtle on dark. */
    public static VBox card(javafx.scene.Node... children) {
        VBox box = new VBox(8);
        box.getChildren().addAll(children);
        box.setPadding(new Insets(14));
        box.setStyle("-fx-background-color: rgba(127,127,127,0.08);"
                + "-fx-background-radius: 12;"
                + "-fx-border-radius: 12;"
                + "-fx-border-color: " + CARD_BORDER + ";"
                + "-fx-border-width: 0.5;");
        return box;
    }

    /** A stat card: big coloured value on top, small caption below.
     *  Used in rows on dashboard-style screens. */
    public static VBox statCard(String value, String captionText, String color) {
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        Label captionLabel = new Label(captionText.toUpperCase());
        captionLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        captionLabel.setStyle("-fx-text-fill: " + GRAY + ";");
        VBox box = new VBox(4, valueLabel, captionLabel);
        box.setPadding(new Insets(14));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: rgba(127,127,127,0.08);"
                + "-fx-background-radius: 12;"
                + "-fx-border-radius: 12;"
                + "-fx-border-color: " + CARD_BORDER + ";"
                + "-fx-border-width: 0.5;");
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    /** A row of stat cards that share the width equally. */
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

    // ---------------------------------------------------------------- Pills

    /** A small rounded badge (statuses, counts, step markers). */
    public static Label pill(String text, String color) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 11));
        label.setStyle("-fx-background-color: " + color + ";"
                + "-fx-text-fill: white;"
                + "-fx-background-radius: 999;"
                + "-fx-padding: 3 10;");
        return label;
    }

    /** A soft pill: tinted background, coloured text (less loud). */
    public static Label softPill(String text, String color) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 11));
        label.setStyle("-fx-background-color: " + tint(color) + ";"
                + "-fx-text-fill: " + color + ";"
                + "-fx-background-radius: 999;"
                + "-fx-padding: 3 10;");
        return label;
    }

    /** Pill colour for an academic / enrollment status word. */
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

    /** A soft pill coloured by status word. */
    public static Label statusPill(String status) {
        return softPill(status, statusColor(status));
    }

    /** Hex colour -> 15% opacity rgba tint for soft pills and selected nav. */
    private static String tint(String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        return "rgba(" + r + "," + g + "," + b + ",0.15)";
    }

    // ------------------------------------------------------------ Progress bar

    /** A thin rounded progress bar built from two Regions (no extra
     *  controls). fraction is clamped to 0..1. */
    public static HBox progressBar(double fraction, String color) {
        double clamped = Math.max(0, Math.min(1, fraction));
        Region filled = new Region();
        filled.setStyle("-fx-background-color: " + color + ";"
                + "-fx-background-radius: 999;");
        filled.setPrefHeight(6);
        filled.setMinHeight(6);
        Region rest = new Region();
        rest.setStyle("-fx-background-color: rgba(127,127,127,0.25);"
                + "-fx-background-radius: 999;");
        rest.setPrefHeight(6);
        rest.setMinHeight(6);
        HBox bar = new HBox(filled, rest);
        double frac = clamped;
        filled.prefWidthProperty().bind(bar.widthProperty().multiply(frac));
        rest.prefWidthProperty().bind(bar.widthProperty().multiply(1 - frac));
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(4, 0, 0, 0));
        return bar;
    }

    // -------------------------------------------------------------- Buttons

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

    /** A left-aligned button for the side navigation (one destination each). */
    public static Button navButton(String text) {
        Button button = new Button(text);
        button.setStyle(navStyle(false));
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    /** Restyle a side navigation button for the selected destination. */
    public static void setNavSelected(Button button, boolean selected) {
        button.setStyle(navStyle(selected));
    }

    private static String navStyle(boolean selected) {
        if (selected) {
            return "-fx-background-color: " + tint(ACCENT) + ";"
                    + "-fx-text-fill: " + ACCENT + ";"
                    + "-fx-font-weight: bold;"
                    + "-fx-background-radius: 8; -fx-padding: 8 12;"
                    + "-fx-alignment: CENTER_LEFT;";
        }
        return "-fx-background-color: transparent;"
                + "-fx-background-radius: 8; -fx-padding: 8 12;"
                + "-fx-alignment: CENTER_LEFT;";
    }

    // ------------------------------------------------------ Status feedback

    /** An empty feedback line; showStatus fills it after an action. */
    public static Label statusLabel() {
        Label label = new Label();
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    /** Fill a status line green (saved) or red (failed) after an action.
     *  A leading word mark keeps the state readable without colour. */
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

    /** A red banner for errors (for example: failed registration rules). */
    public static Label errorBanner(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setStyle("-fx-background-color: " + tint(RED) + ";"
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
        label.setStyle("-fx-background-color: " + tint(GREEN) + ";"
                + "-fx-text-fill: #1E7A1E;"
                + "-fx-background-radius: 10;"
                + "-fx-padding: 10 12;");
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    // ------------------------------------------------------------- Helpers

    /** A grey, centred line for empty lists and empty screens. */
    public static Label emptyState(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: " + GRAY + ";");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setPadding(new Insets(24, 0, 24, 0));
        return label;
    }

    /** Format a CGPA / GPA with two decimals. */
    public static String formatGpa(double value) {
        return String.format("%.2f", value);
    }

    /** A confirmation dialog for irreversible actions; true when confirmed. */
    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait()
                .filter(r -> r == javafx.scene.control.ButtonType.OK)
                .isPresent();
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
