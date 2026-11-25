package ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.CourseSection;
import model.MeetingTime;
import model.Semester;

import java.util.ArrayList;

/**
 * Browse course sections by semester — no sign-in needed,
 * the same as menu option 2 in the console app.
 *
 * Sections show as summary stat cards on top and one row per
 * section in a table underneath, so the screen stays full.
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

        VBox sectionsCard = UiKit.statCard("0", "SECTIONS", UiKit.ACCENT);
        VBox seatsCard = UiKit.statCard("0", "SEATS OPEN", UiKit.GREEN);
        VBox fullCard = UiKit.statCard("0", "FULL", UiKit.RED);
        HBox stats = UiKit.statRow(sectionsCard, seatsCard, fullCard);

        TableView<CourseSection> table = new TableView<>();
        table.setPrefHeight(430);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(UiKit.emptyState("No sections in this semester."));
        table.setStyle("-fx-background-color: white;"
                + "-fx-background-radius: 12;"
                + "-fx-border-color: #D1D1D6;"
                + "-fx-border-radius: 12;"
                + "-fx-border-width: 0.5;");

        TableColumn<CourseSection, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCourse().getCourseCode()
                        + " — " + c.getValue().getCourse().getCourseName()
                        + " (" + c.getValue().getCourse().getCreditHours() + " cr)"));

        TableColumn<CourseSection, String> sectionCol = new TableColumn<>("Section");
        sectionCol.setCellValueFactory(c -> new SimpleStringProperty(
                String.valueOf(c.getValue().getSectionNo())));
        sectionCol.setMaxWidth(1600);
        sectionCol.setMinWidth(60);

        TableColumn<CourseSection, String> instructorCol = new TableColumn<>("Instructor");
        instructorCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getInstructor().getName()));

        TableColumn<CourseSection, String> scheduleCol = new TableColumn<>("Schedule");
        scheduleCol.setCellValueFactory(c -> {
            StringBuilder times = new StringBuilder();
            for (MeetingTime meeting : c.getValue().getMeetings()) {
                if (times.length() > 0) {
                    times.append(", ");
                }
                times.append(meeting);
            }
            return new SimpleStringProperty(times.toString());
        });

        TableColumn<CourseSection, String> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(c -> new SimpleStringProperty(
                seatsLeft(context, c.getValue()) + " / " + c.getValue().getCapacity()));
        seatsCol.setMaxWidth(1600);
        seatsCol.setMinWidth(70);

        TableColumn<CourseSection, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(
                seatsLeft(context, c.getValue()) <= 0 ? "FULL" : "Open"));
        statusCol.setMaxWidth(1600);
        statusCol.setMinWidth(70);

        table.getColumns().setAll(java.util.Arrays.asList(
                courseCol, sectionCol, instructorCol, scheduleCol, seatsCol, statusCol));

        Runnable refresh = () -> {
            Semester semester = semesterBox.getValue();
            ArrayList<CourseSection> sections = new ArrayList<>();
            if (semester != null) {
                sections.addAll(context.getStore().getSectionsInSemester(semester));
            }
            table.setItems(FXCollections.observableArrayList(sections));
            int open = 0;
            int full = 0;
            int seats = 0;
            for (CourseSection section : sections) {
                int left = seatsLeft(context, section);
                if (left <= 0) {
                    full++;
                } else {
                    open++;
                    seats += left;
                }
            }
            Label sectionsValue = (Label) sectionsCard.getChildren().get(0);
            Label seatsValue = (Label) seatsCard.getChildren().get(0);
            Label fullValue = (Label) fullCard.getChildren().get(0);
            sectionsValue.setText(String.valueOf(sections.size()));
            seatsValue.setText(String.valueOf(seats));
            fullValue.setText(String.valueOf(full));
        };
        semesterBox.setOnAction(e -> refresh.run());
        refresh.run();

        Button backButton = UiKit.plainButton("Back to Sign In");
        backButton.setOnAction(e -> app.showLogin());

        VBox root = new VBox(12,
                UiKit.largeTitle("Browse Courses"),
                UiKit.caption("Live seat availability, no sign-in needed."),
                stats,
                UiKit.sectionLabel("Semester"), semesterBox,
                table,
                backButton);
        root.setPadding(new Insets(20));
        return root;
    }

    /** Seats still available in a section (ENROLLED count vs capacity). */
    public static int seatsLeft(AppContext context, CourseSection section) {
        int taken = 0;
        for (model.Student student : context.getStore().getStudents()) {
            for (model.Enrollment enrollment
                    : student.getAcademicRecord().getEnrollments()) {
                if (enrollment.getCourseSection().equals(section)
                        && enrollment.getStatus()
                                == model.enums.EnrollmentStatus.ENROLLED) {
                    taken++;
                }
            }
        }
        return section.getCapacity() - taken;
    }

    /** describe() plus live seat availability; full sections say so up front. */
    public static String describeWithSeats(AppContext context, CourseSection section) {
        int left = seatsLeft(context, section);
        String seats = left <= 0 ? "FULL" : left + " seats left";
        return (left <= 0 ? "[FULL] " : "") + describe(section) + "  •  " + seats;
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
