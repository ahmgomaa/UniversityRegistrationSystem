package ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import model.CourseSection;
import model.MeetingTime;
import model.Semester;

import java.util.ArrayList;

/**
 * Browse course sections by semester — no sign-in needed,
 * the same as menu option 2 in the console app.
 */
public class BrowseView {

    private final AppContext context;
    private final JavaFxApp app;

    public BrowseView(AppContext context, JavaFxApp app) {
        this.context = context;
        this.app = app;
    }

    public VBox build() {
        ArrayList<Semester> semesters = context.getStore().getSemesters();
        ComboBox<Semester> semesterBox = new ComboBox<>(
                FXCollections.observableArrayList(semesters));
        if (!semesters.isEmpty()) {
            semesterBox.setValue(semesters.get(0));
        }
        semesterBox.setMaxWidth(Double.MAX_VALUE);

        ListView<String> sectionsList = new ListView<>();
        sectionsList.setPrefHeight(420);

        Runnable refresh = () -> {
            Semester semester = semesterBox.getValue();
            ArrayList<String> lines = new ArrayList<>();
            if (semester != null) {
                for (CourseSection section : context.getStore().getSectionsInSemester(semester)) {
                    lines.add(describe(section));
                }
            }
            if (lines.isEmpty()) {
                lines.add("No sections in this semester.");
            }
            sectionsList.setItems(FXCollections.observableArrayList(lines));
        };
        semesterBox.setOnAction(e -> refresh.run());
        refresh.run();

        Button backButton = UiKit.plainButton("Back to Sign In");
        backButton.setOnAction(e -> app.showLogin());

        VBox root = new VBox(12,
                UiKit.sectionLabel("Semester"), semesterBox,
                UiKit.sectionLabel("Sections"), sectionsList,
                backButton);
        root.setPadding(new Insets(20));
        return root;
    }

    /** One readable line per section, with its meetings. */
    public static String describe(CourseSection section) {
        StringBuilder text = new StringBuilder();
        text.append(section.getCourse().getCourseCode())
                .append(" — ").append(section.getCourse().getCourseName())
                .append(" (").append(section.getCourse().getCreditHours()).append(" credits)")
                .append("  •  Section ").append(section.getSectionNo())
                .append("  •  ").append(section.getInstructor().getName())
                .append("  •  Capacity ").append(section.getCapacity());
        for (MeetingTime meeting : section.getMeetings()) {
            text.append("  •  ").append(meeting);
        }
        return text.toString();
    }
}
