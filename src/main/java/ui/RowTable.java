package ui;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;

public class RowTable extends ScrollPane {

    public interface SelectionListener {
        void selected(int index);
    }

    private final VBox box = new VBox();
    private final ArrayList<HBox> dataRows = new ArrayList<>();
    private String[] headers = new String[0];
    private double[] weights = new double[0];
    private int selectedIndex = -1;
    private SelectionListener listener;
    private boolean selectable = true;

    public RowTable() {
        getStyleClass().add("row-table");
        setFitToWidth(true);
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setContent(box);
        setMinHeight(120);
        setPrefHeight(220);
    }

    public void setColumns(String[] headers, double[] weights) {
        this.headers = headers;
        this.weights = weights;
        clear();
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public void setSelectionListener(SelectionListener listener) {
        this.listener = listener;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void clear() {
        box.getChildren().clear();
        dataRows.clear();
        selectedIndex = -1;
        if (headers.length > 0) {
            box.getChildren().add(buildRow(headers, true));
            box.getChildren().add(UiKit.separator());
        }
    }

    public void addGroup(String text) {
        Label label = new Label(text);
        label.setFont(UiKit.textFont(FontWeight.BOLD, 13));
        label.getStyleClass().add("table-group");
        label.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().add(label);
    }

    public void addRow(String[] cells) {
        final HBox row = buildRow(cells, false);
        final int index = dataRows.size();
        if (selectable) {
            row.getStyleClass().add("table-row-selectable");
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    select(index);
                }
            });
        }
        dataRows.add(row);
        box.getChildren().add(row);
        box.getChildren().add(UiKit.separator());
    }

    public void showEmpty(String text) {
        box.getChildren().add(UiKit.emptyState(text));
    }

    public void select(int index) {
        for (HBox row : dataRows) {
            row.getStyleClass().remove("table-row-selected");
        }
        if (index < 0 || index >= dataRows.size()) {
            selectedIndex = -1;
            return;
        }
        selectedIndex = index;
        dataRows.get(index).getStyleClass().add("table-row-selected");
        if (listener != null) {
            listener.selected(index);
        }
    }

    private HBox buildRow(String[] cells, boolean header) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(header ? 10 : 11, 16, header ? 10 : 11, 16));
        for (int i = 0; i < cells.length; i++) {
            Label label = new Label(cells[i]);
            double weight = i < weights.length ? weights[i] : 1;
            label.setPrefWidth(weight * 60);
            label.setMinWidth(0);
            label.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(label, Priority.ALWAYS);
            if (header) {
                label.getStyleClass().add("column-header");
            } else {
                label.setFont(UiKit.textFont(13));
                paint(label, cells[i]);
            }
            row.getChildren().add(label);
        }
        return row;
    }

    private void paint(Label label, String text) {
        if (text.equals("ACTIVE") || text.equals("COMPLETED")
                || text.equals("PASSED")) {
            label.setStyle("-fx-text-fill: " + UiKit.GREEN + ";-fx-font-weight: bold;");
        } else if (text.equals("PROBATION") || text.equals("ENROLLED")) {
            label.setStyle("-fx-text-fill: " + UiKit.ORANGE + ";-fx-font-weight: bold;");
        } else if (text.equals("WITHDRAWN") || text.equals("FULL")
                || text.equals("FAILED")) {
            label.setStyle("-fx-text-fill: " + UiKit.RED + ";-fx-font-weight: bold;");
        } else if (text.equals("GRADUATED")) {
            label.setStyle("-fx-text-fill: " + UiKit.GRAY + ";-fx-font-weight: bold;");
        }
    }
}
